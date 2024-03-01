rm release/ -r -Force
mkdir release
./gradlew clean
./gradlew assembleFossRelease
mv app/build/outputs/apk/foss/release/app-foss-release.apk release/
./gradlew clean
./gradlew assemblePlayRelease
mv app/build/outputs/apk/play/release/app-play-release.apk release/
./gradlew bundlePlayRelease
mv app/build/outputs/bundle/playRelease/app-play-release.aab release/
