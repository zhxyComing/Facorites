# 内存泄漏点记录

1. InfiniteBanner 在 onDetachedFromWindow 时要取消 MessageQueue 消息的注册。 -- Handler 内存泄漏
2. Fresco 调用 setImageURI 传入参数 context 时，要使用 Application 而不是 Activity。 -- context 内存泄漏

内存泄漏可能导致崩溃，因为首页的 Fragment 无法被回收，但是其 View 已经被解绑了，所以回调到的组件可能为 null，空指针异常。