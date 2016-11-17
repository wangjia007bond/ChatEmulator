FROM 10.19.132.184:30100/qingyi/android-emulator
MAINTAINER qy

#5901 is exposed for the androidvncserver installed below
EXPOSE 5901
#7344 is exposed for the socket connection
EXPOSE 7344

ADD bin/androidvncserver /androidvncserver
ADD bin/wechat.apk /wechat.apk
ADD bin/app-debug.apk /app-debug.apk

ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
