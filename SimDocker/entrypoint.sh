#!/bin/bash

if [[ $EMULATOR == "" ]]; then
    EMULATOR="android-19"
    echo "Using default emulator $EMULATOR"
fi

if [[ $ARCH == "" ]]; then
    ARCH="x86"
    echo "Using default arch $ARCH"
fi
echo EMULATOR  = "Requested API: ${EMULATOR} (${ARCH}) emulator."
if [[ -n $1 ]]; then
    echo "Last line of file specified as non-opt/last argument:"
    tail -1 $1
fi

# Run sshd
/usr/sbin/sshd

# Detect ip and forward ADB ports outside to outside interface
ip=$(ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' | cut -d: -f2 | awk '{ print $1}')
socat tcp-listen:5037,bind=$ip,fork tcp:127.0.0.1:5037 &
socat tcp-listen:5554,bind=$ip,fork tcp:127.0.0.1:5554 &
socat tcp-listen:5555,bind=$ip,fork tcp:127.0.0.1:5555 &
socat tcp-listen:5901,bind=$ip,fork tcp:127.0.0.1:5901 &
socat tcp-listen:7344,bind=$ip,fork tcp:127.0.0.1:7344 &

# Set up and run emulator
if [[ $ARCH == *"x86"* ]]
then
    EMU="x86"
else
    EMU="arm"
fi

echo "no" | /usr/local/android-sdk/tools/android create avd -f -n test -t ${EMULATOR} --abi default/${ARCH}
echo "no" | /usr/local/android-sdk/tools/emulator64-${EMU} -avd test -noaudio -no-window -gpu off -verbose -qemu -usbdevice tablet -vnc :0 &

#my initial code
adb devices
sleep 120s

adb install wechat.apk

adb forward tcp:7344 tcp:7344
adb install app-debug.apk

sleep 5s
echo "start app........"
adb shell am start -n com.enn.cloud.wechatdevopsbot/com.enn.cloud.wechatdevopsbot.MainActivity
adb shell am start -n com.tencent.mm/com.tencent.mm.ui.LauncherUI
echo "finished......."

adb push /androidvncserver /data/
adb shell chmod 755 /data/androidvncserver
adb forward tcp:5901 tcp:5901
adb shell /data/androidvncserver -k /dev/input/event0 -t /dev/input/event0
