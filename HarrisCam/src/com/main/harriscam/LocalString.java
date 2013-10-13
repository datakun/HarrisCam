package com.main.harriscam;

public class LocalString {
	private static final String ENGLISH = "en_US";
	private static final String KOREAN = "ko_KR";

	private static int nLanguage = 0;

	private static final String allYes[] = { "Yes", "��" };
	private static final String allNo[] = { "No", "�ƴϿ�" };
	private static final String allAbout[] = {
			"Harris Cam is a camera application that can be applied to the Harris Shutter Effect.\r\n\r\nHarris Shutter effects combine to make pictures with layers of different colors of photos taken at different times. Can get interesting photo when taking pictures at various time intervals. You also can take a picture you want to apply the effect directly, without time interval.\r\n\r\nNow. take a photo around you and show your friend!\r\n\r\n- datakun",
			"Harris Cam �� Harris Shutter ȿ���� ������ �� �ִ� ī�޶� �����Դϴ�.\r\n\r\nHarris Shutter �� �ð� ������ �ΰ� ���� ������ ���� �ٸ� ���� ���̾�� ����� ��ġ�� ȿ���� ���մϴ�. �پ��� �ð� ������ �ΰ� ������ ������ ��մ� ����� ���� �� �ֽ��ϴ�. ���� �ð� ���ݿ� ���ָ� ���� �ʰ� ���� ȿ���� ������ ������ ���� ���� �ֽ��ϴ�.\r\n\r\n���� ���� �ڽ��� �ֺ��� ��������!\r\n\r\n- datakun" };

	private static final String cropApplySuccess[] = { "Apply the effect.", "ȿ�� ���� �Ϸ�." };
	private static final String cropShareMessage[] = { "Select an App to send.", "������ ���� �����ϼ���." };
	private static final String cropImageSave[] = { "Do you want to save?", "������ �����Ͻðڽ��ϱ�?" };
	private static final String cropImageCancel[] = { "Do you want to cancel?", "�۾��� ����Ͻðڽ��ϱ�?" };

	private static final String mainUpdating[] = { "Updating Latest Version", "�ֽ� ���� ������Ʈ" };
	private static final String mainUpdateAsking[] = {
			"Do you want to update the latest version? (If you want to off this alarm. Click 'No')",
			"�ֽ� �������� ������Ʈ �Ͻðڽ��ϱ�? (�ֽ� ���� �˶��� ������ '�ƴϿ�'�� ��������.)" };
	private static final String mainSaveCancel[] = { "Save canceled...", "���� ���..." };
	private static final String mainSaveSuccess[] = { "Save successful!", "���� ����!" };
	private static final String mainSaveFailed[] = { "Apply failed. Try again.", "�ٽ� �õ����ּ���." };
	private static final String mainFinish[] = { "Do you want to close this app? One more 'Back'", "�����Ͻðڽ��ϱ�? '����'��ư�� �� �� �� ��������." };
	private static final String mainApplyHarris[] = { "Applying the Effect...", "ȿ�� ���� ��..." };

	private static final String setLVersion[] = { "This is the latest version.", "�ֽ� �����Դϴ�." };
	private static final String setUpdateAsking[] = { "Do you want to update the latest version?", "�ֽ� �������� ������Ʈ �Ͻðڽ��ϱ�?" };

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
