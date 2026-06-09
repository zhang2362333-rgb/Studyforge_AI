package com.studyforge.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studyforge.common.enums.RoleType;
import com.studyforge.common.enums.UserStatus;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.dto.LoginRequest;
import com.studyforge.system.dto.RegisterRequest;
import com.studyforge.system.entity.User;
import com.studyforge.system.entity.UserToken;
import com.studyforge.system.mapper.UserExperienceMapper;
import com.studyforge.system.mapper.UserMapper;
import com.studyforge.system.mapper.UserTokenMapper;
import com.studyforge.system.vo.LoginVO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserExperienceMapper userExperienceMapper;

    @Mock
    private UserTokenMapper userTokenMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUserId(1L);
        activeUser.setUsername("alice");
        activeUser.setDisplayName("Alice");
        activeUser.setEmail("alice@example.com");
        activeUser.setPasswordHash(sha256("secret123"));
        activeUser.setRole(RoleType.USER);
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setCommunityLevel(2);
        activeUser.setExperiencePoints(120);
    }

    // 与生产实现保持一致的哈希，用于构造测试数据
    private static String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return "sha256:" + HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private UserToken token(String value, String status, LocalDateTime expire) {
        UserToken t = new UserToken();
        t.setUserId(1L);
        t.setAccessToken(value);
        t.setStatus(status);
        t.setExpireTime(expire);
        return t;
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("正确账号密码登录成功并签发 token")
        void loginSuccess() {
            when(userMapper.selectByAccount("alice")).thenReturn(activeUser);
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            LoginVO vo = authService.login(new LoginRequest("alice", "secret123"));

            assertThat(vo.userId()).isEqualTo(1L);
            assertThat(vo.username()).isEqualTo("alice");
            assertThat(vo.accessToken()).startsWith("sf_");
            verify(userTokenMapper).insert(any(UserToken.class));
        }

        @Test
        @DisplayName("账号前后空白会被裁剪")
        void loginTrimsAccount() {
            when(userMapper.selectByAccount("alice")).thenReturn(activeUser);
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            authService.login(new LoginRequest("  alice  ", "secret123"));

            verify(userMapper).selectByAccount("alice");
        }

        @Test
        @DisplayName("密码错误抛 UNAUTHORIZED")
        void loginWrongPassword() {
            when(userMapper.selectByAccount("alice")).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong-password")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
            verify(userTokenMapper, never()).insert(any());
        }

        @Test
        @DisplayName("账号不存在抛 UNAUTHORIZED")
        void loginUnknownAccount() {
            when(userMapper.selectByAccount("ghost")).thenReturn(null);

            assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "secret123")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("非 ACTIVE 用户禁止登录")
        void loginDisabledUser() {
            activeUser.setStatus(UserStatus.DISABLED);
            when(userMapper.selectByAccount("alice")).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "secret123")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("缺少账号或密码抛 VALIDATION_ERROR")
        void loginMissingFields() {
            assertThatThrownBy(() -> authService.login(new LoginRequest("", "secret123")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
            assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
        }

        @Test
        @DisplayName("首次每日登录发放经验奖励")
        void loginGrantsDailyReward() {
            activeUser.setLastLoginRewardDate(LocalDate.now().minusDays(1));
            when(userMapper.selectByAccount("alice")).thenReturn(activeUser);
            when(userMapper.rewardDailyLogin(eq(1L), any(LocalDate.class), anyInt())).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            LoginVO vo = authService.login(new LoginRequest("alice", "secret123"));

            assertThat(vo.dailyRewardApplied()).isTrue();
            assertThat(vo.dailyExperienceDelta()).isEqualTo(15);
            verify(userExperienceMapper).insertIgnore(eq(1L), eq("DAILY_LOGIN"), eq(15), any(), any(LocalDate.class));
        }

        @Test
        @DisplayName("当天已领取则不重复发放奖励")
        void loginNoDoubleRewardSameDay() {
            activeUser.setLastLoginRewardDate(LocalDate.now());
            when(userMapper.selectByAccount("alice")).thenReturn(activeUser);
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            LoginVO vo = authService.login(new LoginRequest("alice", "secret123"));

            assertThat(vo.dailyRewardApplied()).isFalse();
            assertThat(vo.dailyExperienceDelta()).isZero();
            verify(userMapper, never()).rewardDailyLogin(anyLong(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("合法注册成功并以 sha256 前缀存储哈希、邮箱小写")
        void registerSuccess() {
            when(userMapper.selectByAccount(any())).thenReturn(null);
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            authService.register(new RegisterRequest("bob_99", "Bob@Example.com", "password1"));

            verify(userMapper).insert(captor.capture());
            User saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("bob_99");
            assertThat(saved.getEmail()).isEqualTo("bob@example.com");
            assertThat(saved.getPasswordHash()).startsWith("sha256:");
            assertThat(saved.getRole()).isEqualTo(RoleType.USER);
            assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("用户名非法（含非法字符/过短）抛 VALIDATION_ERROR")
        void registerInvalidUsername() {
            assertThatThrownBy(() -> authService.register(new RegisterRequest("ab", "bob@example.com", "password1")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
            assertThatThrownBy(() -> authService.register(new RegisterRequest("bad name!", "bob@example.com", "password1")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
        }

        @Test
        @DisplayName("邮箱格式非法抛 VALIDATION_ERROR")
        void registerInvalidEmail() {
            assertThatThrownBy(() -> authService.register(new RegisterRequest("bob_99", "not-an-email", "password1")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
        }

        @Test
        @DisplayName("密码过短抛 VALIDATION_ERROR")
        void registerShortPassword() {
            assertThatThrownBy(() -> authService.register(new RegisterRequest("bob_99", "bob@example.com", "short")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
        }

        @Test
        @DisplayName("账号已存在抛 VALIDATION_ERROR")
        void registerDuplicateAccount() {
            when(userMapper.selectByAccount("bob_99")).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.register(new RegisterRequest("bob_99", "bob@example.com", "password1")))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.VALIDATION_ERROR.getCode());
            verify(userMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("token 校验与权限")
    class TokenAndAuthorization {

        @Test
        @DisplayName("requireUser 接受带 Bearer 前缀的有效 token")
        void requireUserAcceptsBearer() {
            when(userTokenMapper.selectByToken("tok-1"))
                    .thenReturn(token("tok-1", "ACTIVE", LocalDateTime.now().plusDays(1)));
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            User user = authService.requireUser("Bearer tok-1");

            assertThat(user.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("空 Authorization 抛 UNAUTHORIZED")
        void requireUserBlankToken() {
            assertThatThrownBy(() -> authService.requireUser("   "))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("已撤销 token 抛 UNAUTHORIZED")
        void requireUserRevokedToken() {
            when(userTokenMapper.selectByToken("tok-1"))
                    .thenReturn(token("tok-1", "REVOKED", LocalDateTime.now().plusDays(1)));

            assertThatThrownBy(() -> authService.requireUser("Bearer tok-1"))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("过期 token 抛 UNAUTHORIZED")
        void requireUserExpiredToken() {
            when(userTokenMapper.selectByToken("tok-1"))
                    .thenReturn(token("tok-1", "ACTIVE", LocalDateTime.now().minusMinutes(1)));

            assertThatThrownBy(() -> authService.requireUser("Bearer tok-1"))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("currentUserId 对无效 token 返回 null 而非抛异常")
        void currentUserIdReturnsNullForInvalid() {
            assertThat(authService.currentUserId(null)).isNull();
            when(userTokenMapper.selectByToken("bad")).thenReturn(null);
            assertThat(authService.currentUserId("Bearer bad")).isNull();
        }

        @Test
        @DisplayName("requireAdmin 对普通用户抛 FORBIDDEN")
        void requireAdminRejectsRegularUser() {
            when(userTokenMapper.selectByToken("tok-1"))
                    .thenReturn(token("tok-1", "ACTIVE", LocalDateTime.now().plusDays(1)));
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.requireAdmin("Bearer tok-1"))
                    .isInstanceOf(BizException.class)
                    .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("requireAdmin 放行管理员")
        void requireAdminAllowsAdmin() {
            activeUser.setRole(RoleType.ADMIN);
            when(userTokenMapper.selectByToken("tok-1"))
                    .thenReturn(token("tok-1", "ACTIVE", LocalDateTime.now().plusDays(1)));
            when(userMapper.selectById(1L)).thenReturn(activeUser);

            authService.requireAdmin("Bearer tok-1");
        }
    }

    @Test
    @DisplayName("logout 将 token 状态置为 REVOKED")
    void logoutRevokesToken() {
        authService.logout("Bearer tok-1");
        verify(userTokenMapper).updateStatusByToken("tok-1", "REVOKED");
    }
}
