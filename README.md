the repo to host the DevOps BOT, This repo have four projects, SimDocker SocketClient SocketServer and WechatDevOpsBot.

1. SimDocker is a Dockerfile, you can use it to build a image which has a android emulator, wechat and accessibility app.
use the command to run 
cd /SimDocker
docker build -t="wangjia007bond/android" .
docker run -d -p 5901:5901 -p 7344:7344  -v /tmp:/data -P -e "EMULATOR=android-19" -e "ARCH=armeabi-v7a" --name android wangjia007bond/android

2. WechatDevOpsBot is a android application, it monitor wechat view and communicate with server. You can open it from android studio, and build apk, copy this apk to SimDocker/bin folder.
WechatDevOpsBot has a view to allow you to test the connection to client.

3. SocketClient and SocketServer are socket application, to allow you to test with WechatDevOpsBot.





