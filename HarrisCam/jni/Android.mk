LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := imgeffects
LOCAL_SRC_FILES := imgeffects.c
LOCAL_LDLIBS    := -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
