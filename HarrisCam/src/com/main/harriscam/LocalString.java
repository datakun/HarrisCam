package com.main.harriscam;

public class LocalString {
	private static final String ENGLISH = "en_US";
	private static final String KOREAN = "ko_KR";

	private static int nLanguage = 0;

	private static final String allYes[] = { "Yes", "네" };
	private static final String allNo[] = { "No", "아니오" };
	private static final String allAbout[] = {
			"Harris Cam is a camera application that can be applied to the Harris Shutter Effect.\r\n\r\nHarris Shutter effects combine to make pictures with layers of different colors of photos taken at different times. Can get interesting photo when taking pictures at various time intervals. You also can take a picture you want to apply the effect directly, without time interval.\r\n\r\nNow. take a photo around you and show your friend!\r\n\r\n- datakun",
			"Harris Cam 은 Harris Shutter 효과를 적용할 수 있는 카메라 어플입니다.\r\n\r\nHarris Shutter 는 시간 간격을 두고 찍은 사진을 각각 다른 색의 레이어로 만들어 합치는 효과를 말합니다. 다양한 시간 간격을 두고 사진을 찍으면 재밌는 결과를 얻을 수 있습니다. 또한 시간 간격에 구애를 받지 않고 직접 효과를 적용할 사진을 찍을 수도 있습니다.\r\n\r\n지금 당장 자신의 주변을 찍으세요!\r\n\r\n- datakun" };

	private static final String cropApplySuccess[] = { "Apply the effect.", "효과 적용 완료." };
	private static final String cropShareMessage[] = { "Select an App to send.", "공유할 앱을 선택하세요." };
	private static final String cropImageSave[] = { "Do you want to save?", "사진을 저장하시겠습니까?" };
	private static final String cropImageCancel[] = { "Do you want to cancel?", "작업을 취소하시겠습니까?" };

	private static final String mainUpdating[] = { "Updating Latest Version", "최신 버전 업데이트" };
	private static final String mainUpdateAsking[] = {
			"Do you want to update the latest version? (If you want to off this alarm. Click 'No')",
			"최신 버전으로 업데이트 하시겠습니까? (최신 버전 알람을 끄려면 '아니오'를 누르세요.)" };
	private static final String mainSaveCancel[] = { "Save canceled...", "저장 취소..." };
	private static final String mainSaveSuccess[] = { "Save successful!", "저장 성공!" };
	private static final String mainSaveFailed[] = { "Apply failed. Try again.", "다시 시도해주세요." };
	private static final String mainFinish[] = { "Do you want to close this app? One more 'Back'", "종료하시겠습니까? '이전'버튼을 한 번 더 누르세요." };
	private static final String mainApplyHarris[] = { "Applying the Effect...", "효과 적용 중..." };

	private static final String setLVersion[] = { "This is the latest version.", "최신 버전입니다." };
	private static final String setUpdateAsking[] = { "Do you want to update the latest version?", "최신 버전으로 업데이트 하시겠습니까?" };

	public String aYes;
	public String aNo;
	public String aAbout;

	public String cApplySuccess;
	public String cShareMessage;
	public String cImageSave;
	public String cImageCancel;

	public String mUpdating;
	public String mUpdateAsking;
	public String mSaveCancel;
	public String mSaveSuccess;
	public String mSaveFailed;
	public String mFinish;
	public String mApplyHarris;

	public String sLVersion;
	public String sUpdateAsking;

	public LocalString(String strLanguage) {
		if (strLanguage.equals(KOREAN)) {
			nLanguage = 1;
		} else {
			nLanguage = 0;
		}

		aYes = allYes[nLanguage];
		aNo = allNo[nLanguage];
		aAbout = allAbout[nLanguage];

		cApplySuccess = cropApplySuccess[nLanguage];
		cShareMessage = cropShareMessage[nLanguage];
		cImageSave = cropImageSave[nLanguage];
		cImageCancel = cropImageCancel[nLanguage];

		mUpdating = mainUpdating[nLanguage];
		mUpdateAsking = mainUpdateAsking[nLanguage];
		mSaveCancel = mainSaveCancel[nLanguage];
		mSaveSuccess = mainSaveSuccess[nLanguage];
		mSaveFailed = mainSaveFailed[nLanguage];
		mFinish = mainFinish[nLanguage];
		mApplyHarris = mainApplyHarris[nLanguage];

		sLVersion = setLVersion[nLanguage];
		sUpdateAsking = setUpdateAsking[nLanguage];
	}
}
