#include <jni.h>
#include <string>
#include "SnapCore.hpp"

extern "C" JNIEXPORT jstring JNICALL
Java_com_snapstudio_app_MainActivity_getCoreVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = snapstudio::SnapCore::getEngineVersion();
    return env->NewStringUTF(version.c_str());
}
