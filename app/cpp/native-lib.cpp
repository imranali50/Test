#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring

JNICALL
Java_com_locate_locationtracker_adManager_utils_Constant_getMain(JNIEnv *env, jobject clazz) {
      std::string hello = "https://findmylocationtracker.in/FamilyConnectNew/V1/";
//    std::string hello = "https://findmylocationtracker.in/GZ409_FamilyConnect/V1/";
//    std::string hello = "http://142.93.246.162/demoadsnew/V1/";


    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_ad_adlib_Encrypt_DecScript_getDecKey1(JNIEnv *env, jobject thiz) {
    std::string hello = "8867974FE842CAD6243ADDBFCF4DEE350CC8EE72A457C3AAA35F79627FD13728";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_ad_adlib_Encrypt_DecScript_getDecKey2(JNIEnv *env, jobject thiz) {
    std::string hello = "76B9304AABFB57B0A4AE8070BCBB39070CC8EE72A457C3AAA35F79627FD13728";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_ad_adlib_Encrypt_EncScript_getEncKey1(JNIEnv *env, jobject thiz) {
    std::string hello = "8867974FE842CAD6243ADDBFCF4DEE350CC8EE72A457C3AAA35F79627FD13728";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_ad_adlib_Encrypt_EncScript_getEncKey2(JNIEnv *env, jobject thiz) {
    std::string hello = "76B9304AABFB57B0A4AE8070BCBB39070CC8EE72A457C3AAA35F79627FD13728";
    return env->NewStringUTF(hello.c_str());
}