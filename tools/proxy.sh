#!/bin/sh

adb reverse --remove-all
adb reverse tcp:12443 tcp:12443 #as-mock
adb reverse tcp:13443 tcp:13443 #issuer-poc
adb reverse tcp:15443 tcp:15443 #rb-be-mock
adb reverse tcp:14443 tcp:14443 #rp-mock
#adb reverse tcp:16443 tcp:16443 #wallet-mock
adb reverse tcp:8080 tcp:8080
adb reverse tcp:8081 tcp:8081
adb reverse tcp:4200 tcp:4200
adb reverse tcp:6565 tcp:6565
adb reverse tcp:6543 tcp:6543