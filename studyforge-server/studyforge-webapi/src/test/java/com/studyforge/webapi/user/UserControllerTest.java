package com.studyforge.webapi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studyforge.content.service.PostQueryService;
import com.studyforge.system.service.AuthService;
import com.studyforge.system.service.UserProfileService;
import com.studyforge.system.vo.UserProfileVO;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.PostMapping;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private PostQueryService postQueryService;

    @Test
    void unfollowCompatibilityUsesExpectedRouteAndDelegatesToService() throws NoSuchMethodException {
        UserController controller = new UserController(authService, userProfileService, postQueryService);
        UserProfileVO profile = new UserProfileVO(
                8L,
                "target",
                "target@example.com",
                "Target User",
                "",
                "",
                "",
                1,
                10,
                50,
                0,
                4,
                1,
                2,
                3,
                1,
                0,
                6,
                12,
                false,
                "NONE",
                null,
                false
        );
        when(authService.requireUserId("Bearer viewer-token")).thenReturn(3L);
        when(userProfileService.unfollow(3L, 8L)).thenReturn(profile);

        var response = controller.unfollowCompatibility("Bearer viewer-token", 8L);

        verify(authService).requireUserId("Bearer viewer-token");
        verify(userProfileService).unfollow(3L, 8L);
        assertThat(response.getData()).isEqualTo(profile);

        Method method = UserController.class.getMethod("unfollowCompatibility", String.class, Long.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).containsExactly("/{userId}/unfollow");
    }
}
