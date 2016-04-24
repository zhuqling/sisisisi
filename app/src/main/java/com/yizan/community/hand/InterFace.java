package com.yizan.community.hand;

public interface InterFace {
	public final static int EC_UNLOGIN = 99996; //未登录
	/**
	 * 返回值状态码
	 * 
	 * @author Altas
	 * @email Altas.Tutu@gmail.com
	 * @time Dec 26, 2014 10:14:09 AM
	 */
	public enum ResponseCode {
		SUCCESS(0, "成功"), FAILURE(1, "网络请求错误"), ERROR_UNLOGIN(EC_UNLOGIN, "未登录"), ERROR_TOKEN(99997, "TOKEN错误"), ERROR_APP_SECRET(99998, "安全错误"), ERROR_SERVER(99999, "程序处理错误");
		public int code;
		public String msg;

		private ResponseCode(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public static String getMsg(int code) {
			for (ResponseCode rc : ResponseCode.values()) {
				if (rc.code == code) {
					return rc.msg;
				}
			}
			return "";
		}
	}

	/**
	 * 订单状态
	 * 
	 * @author Altas
	 * @email Altas.Tutu@gmail.com
	 * @time 2015-4-1 下午5:55:44
	 */
	public enum ORDER_STATE {
		NO_PAY(0, "等待支付"), CANCEL(1, "已取消"), SERVICE_WAIT(2, "等待服务"), SERVICE_ACTION(3, "服务人员已出发"), SERVICE_PROGRESS(4, "服务进行中"), IS_OK(5, "待确认"), EVA_WAIT(6, "待评价"), COMPLETE(7,
				"服务完成");
		public int code;
		public String type;

		private ORDER_STATE(int code, String msg) {
			this.code = code;
			this.type = msg;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getType() {
			return type;
		}

		public void setType(String msg) {
			this.type = msg;
		}

		public static String getType(int code) {
			for (ORDER_STATE rc : ORDER_STATE.values()) {
				if (rc.code == code) {
					return rc.type;
				}
			}
			return "";
		}
	}
	/**
	 * 评价状态
	 * @author Altas
	 * @email Altas.Tutu@gmail.com
	 * @time 2015-4-2 上午11:32:05
	 */
	public enum EVALUATE_STATE {
		UP("good", "好评"), DOWN("bad", "差评"), MIDDLE("neutral", "中评");
		public String code;
		public String type;

		private EVALUATE_STATE(String code, String msg) {
			this.code = code;
			this.type = msg;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getType() {
			return type;
		}

		public void setType(String msg) {
			this.type = msg;
		}

		public static String getType(String code) {
			for (EVALUATE_STATE rc : EVALUATE_STATE.values()) {
				if (rc.code.equals( code)) {
					return rc.type;
				}
			}
			return "";
		}
	}
}

