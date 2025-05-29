package com.example.video.player

sealed class PlayerStatus {
    //空闲状态，只是初始化了对象，还没有设置播放参数
    data object IDEL : PlayerStatus()

    //设置了播放参数，包括地址，回调等
    data object INITIALIZED : PlayerStatus()

    //正在准备资源，如:读取流文件中
    data object PREPARING : PlayerStatus()

    //资源准备完成，可以播放，还未播放
    data object PREPARED : PlayerStatus()

    //第一次播放中的状态
    data object PLAYING : PlayerStatus()

    //第一次播放时暂停阶段，没有播放状态
    data object PAUSED : PlayerStatus()

    //准备开始播放，并没有直接调用play函数(缓冲的时候点击播放)
    data object PENDING_PLAY : PlayerStatus()

    //循环播放状态
    data object LOOP_PLAYING_BACK : PlayerStatus()

    //销毁
    data object RELEASED : PlayerStatus()

}