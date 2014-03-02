LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := HarrisCam
LOCAL_SRC_FILES := HarrisCam.c
LOCAL_LDLIBS    := -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
