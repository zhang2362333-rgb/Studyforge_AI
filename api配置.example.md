# API 配置示例

这个文件只保留配置项说明，不要填写真实密钥。真实配置请放在本地 `api配置.md`、服务器环境变量、服务器外部配置文件或后台集成配置中。

## 对话/文本模型

```text
model: deepseek-ai/DeepSeek-V4-Flash
base_url: https://api.siliconflow.cn/v1
api_key: <AI_API_KEY>
```

## 语音模型

```text
model: FunAudioLLM/CosyVoice2-0.5B
base_url: https://api.siliconflow.cn/v1
api_key: <VOICE_API_KEY>
voice: FunAudioLLM/CosyVoice2-0.5B:alex
```

## 生图配置

```text
model: gpt-image-2
base_url: https://api.hiyo.top/v1
api_key: <IMAGE_API_KEY>
size: 1536x1024
```
