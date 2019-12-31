package com.feiniu.yx.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PageColorUtil {
	private static JSONArray  color = new JSONArray();
	static {
		PageColorUtil p = new PageColorUtil();
		color.add(p.qunqing());
		color.add(p.nuse());
		color.add(p.qinglu());
		color.add(p.ruzhu());
		color.add(p.luse());
		color.add(p.bohelu());
		color.add(p.shenlu());
		color.add(p.caolu());
		color.add(p.zihong());
		color.add(p.hongse());
		color.add(p.hongchi());
		color.add(p.fenhong());
		color.add(p.hongmei());
		color.add(p.juhong());
		color.add(p.chengse());
		color.add(p.yujing());
		color.add(p.huangse());
		color.add(p.ziluolan());
		color.add(p.qingzi());
		color.add(p.zise());
		color.add(p.modan());
		color.add(p.kafei());
		color.add(p.zheshi());
	}
	
	/**
	 * 群青
	 */
	private JSONObject qunqing(){
		JSONObject res = new JSONObject();
		res.put("name", "群青");
		res.put("index", "1");
		String[] colorParam = {"#DDECFB,#CEE5FD,#489CF6,#E5F2FF,85%,#2977CC,#4B89DA,#4B89DA,#FFFFFF,#CEE5FD,#489CF6,#FFB33A",
		                       "#DDECFB,#73B1F4,#FFFFFF,#73B1F4,85%,#FFEC00,#4B89DA,#4B89DA,#FFFFFF,#CEE5FD,#489CF6,#FFB33A",
		                       "#DDECFB,#5E9CEA,#FFFFFF,#5E9CEA,85%,#FFEC00,#4B89DA,#4B89DA,#FFFFFF,#CEE5FD,#489CF6,#FFB33A",
		                       "#DDECFB,#4B89DA,#FFFFFF,#4B89DA,85%,#FFEC00,#4B89DA,#4B89DA,#FFFFFF,#CEE5FD,#489CF6,#FFB33A",
		                       "#5E9CEA,#FFFFFF,#5E9CEA,#FFFFFF,85%,#FF8000,#FFFFFF,#546ADA,#FFFFFF,#f2f2f2,#489CF6,#FFB33A",
		                       "#4B89DA,#FFFFFF,#5E9CEA,#FFFFFF,85%,#FF8000,#FFFFFF,#546ADA,#FFFFFF,#f2f2f2,#489CF6,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 蓝色
	 */
	private JSONObject nuse(){
		JSONObject res = new JSONObject();
		res.put("name", "蓝色");
		res.put("index", "2");
		String[] colorParam = {"#E9F4FC,#C4E5F6,#029DE4,#D8F2FF,85%,#016FB8,#0399EA,#0399EA,#FFFFFF,#C4E5F6,#029DE4,#FFB33A",
								"#E9F4FC,#51C6FC,#FFFFFF,#51C6FC,85%,#FBE193,#0399EA,#0399EA,#FFFFFF,#C4E5F6,#029DE4,#FFB33A",
								"#E9F4FC,#03A9F5,#FFFFFF,#03A9F5,85%,#FEE392,#0399EA,#0399EA,#FFFFFF,#C4E5F6,#029DE4,#FFB33A",
								"#E9F4FC,#0399EA,#FFFFFF,#0399EA,85%,#FFD96A,#0399EA,#0399EA,#FFFFFF,#C4E5F6,#029DE4,#FFB33A",
								"#03A9F5,#FFFFFF,#0399EA,#FFFFFF,85%,#FF8000,#FFFFFF,#3D57D8,#FFFFFF,#f2f2f2,#029DE4,#FFB33A",
								"#0399EA,#FFFFFF,#0399EA,#FFFFFF,85%,#FF8000,#FFFFFF,#3D57D8,#FFFFFF,#f2f2f2,#029DE4,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 青绿
	 * @return
	 */
	private JSONObject qinglu(){
		JSONObject res = new JSONObject();
		res.put("name", "青绿");
		res.put("index", "3");
		String[] colorParam = {"#EDF9F9,#B0E7E4,#2E9B95,#D1FCF9,85%,#185B58,#03ADA2,#03ADA2,#FFFFFF,#B0E7E4,#3FA5A0,#FFB33A",
								"#EDF9F9,#50CFC7,#FFFFFF,#50CFC7,85%,#DEFF00,#03ADA2,#03ADA2,#FFFFFF,#B0E7E4,#3FA5A0,#FFB33A",
								"#EDF9F9,#16C1B6,#FFFFFF,#16C1B6,85%,#DEFF00,#03ADA2,#03ADA2,#FFFFFF,#B0E7E4,#3FA5A0,#FFB33A",
								"#EDF9F9,#03ADA2,#FFFFFF,#03ADA2,85%,#DEFF00,#03ADA2,#03ADA2,#FFFFFF,#B0E7E4,#3FA5A0,#FFB33A",
								"#16C1B6,#FFFFFF,#16C1B6,#FFFFFF,85%,#00635D,#FFFFFF,#00A096,#FFFFFF,#E1FCFA,#3FA5A0,#FFB33A",
								"#03ADA2,#FFFFFF,#03ADA2,#FFFFFF,85%,#00635D,#FFFFFF,#00A096,#FFFFFF,#E1FCFA,#3FA5A0,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 若竹
	 * @return
	 */
	private JSONObject ruzhu(){
		JSONObject res = new JSONObject();
		res.put("name", "若竹");
		res.put("index", "4");
		String[] colorParam = {"#E3F8F2,#C0F3E6,#3A9B76,#D8FFF5,85%,#2BC69E,#36BA9B,#36BA9B,#FFFFFF,#C0F3E6,#2BC69E,#FFB33A",
								"#E3F8F2,#62DDBD,#FFFFFF,#62DDBD,85%,#FEF548,#36BA9B,#36BA9B,#FFFFFF,#C0F3E6,#2BC69E,#FFB33A",
								"#E3F8F2,#46CEAD,#FFFFFF,#46CEAD,85%,#FFEC00,#36BA9B,#36BA9B,#FFFFFF,#C0F3E6,#2BC69E,#FFB33A",
								"#E3F8F2,#36BA9B,#FFFFFF,#36BA9B,85%,#FFEC00,#36BA9B,#36BA9B,#FFFFFF,#C0F3E6,#2BC69E,#FFB33A",
								"#46CEAD,#FFFFFF,#46CEAD,#FFFFFF,85%,#00635D,#FFFFFF,#00B48A,#FFFFFF,#E5F8F3,#46CEAD,#FFB33A",
								"#36BA9B,#FFFFFF,#36BA9B,#FFFFFF,85%,#00635D,#FFFFFF,#009472,#FFFFFF,#E5F8F3,#46CEAD,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 绿色
	 * @return
	 */
	private JSONObject luse(){
		JSONObject res = new JSONObject();
		res.put("name", "绿色");
		res.put("index", "5");
		String[] colorParam = {"#EBF8ED,#B7EFBC,#437C4B,#C8FBCC,85%,#014E0C,#22A836,#22A836,#FFFFFF,#B7EFBC,#4D8656,#FFB33A",
								"#EBF8ED,#6FD67F,#FFFFFF,#6FD67F,85%,#DEFF00,#22A836,#22A836,#FFFFFF,#B7EFBC,#4D8656,#FFB33A",
								"#EBF8ED,#42BC54,#FFFFFF,#42BC54,85%,#DEFF00,#22A836,#22A836,#FFFFFF,#B7EFBC,#4D8656,#FFB33A",
								"#EBF8ED,#22A836,#FFFFFF,#22A836,85%,#DEFF00,#22A836,#22A836,#FFFFFF,#B7EFBC,#4D8656,#FFB33A",
								"#42BC54,#FFFFFF,#42BC54,#FFFFFF,85%,#FF8000,#FFFFFF,#008B15,#FFFFFF,#E0FFE3,#42BC54,#FFB33A",
								"#22A836,#FFFFFF,#22A836,#FFFFFF,85%,#FF8000,#FFFFFF,#008B15,#FFFFFF,#E0FFE3,#42BC54,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 薄荷绿
	 * @return
	 */
	private JSONObject bohelu(){
		JSONObject res = new JSONObject();
		res.put("name", "薄荷绿");
		res.put("index", "6");
		String[] colorParam = {"#E2F9D8,#C4EFAA,#53A51A,#DCFBC9,85%,#164F00,#3A9417,#3A9417,#FFFFFF,#C4EFAA,#53A51A,#FFB33A",
								"#E2F9D8,#82BD42,#FFFFFF,#82BD42,85%,#FEE28F,#3A9417,#3A9417,#FFFFFF,#C4EFAA,#53A51A,#FFB33A",
								"#E2F9D8,#65B130,#FFFFFF,#65B130,85%,#FFF034,#3A9417,#3A9417,#FFFFFF,#C4EFAA,#53A51A,#FFB33A",
								"#E2F9D8,#3A9417,#FFFFFF,#3A9417,85%,#FFF034,#3A9417,#3A9417,#FFFFFF,#C4EFAA,#53A51A,#FFB33A",
								"#65B130,#FFFFFF,#65B130,#FFFFFF,85%,#FF8000,#FFFFFF,#30880E,#FFFFFF,#DFFBCE,#65B130,#FFB33A",
								"#3A9417,#FFFFFF,#3A9417,#FFFFFF,85%,#FF8000,#FFFFFF,#29770B,#FFFFFF,#DFFBCE,#65B130,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 深绿
	 * @return
	 */
	private JSONObject shenlu(){
		JSONObject res = new JSONObject();
		res.put("name", "深绿");
		res.put("index", "7");
		String[] colorParam = {"#D8FCD8,#B3F6B2,#43BD41,#D1FFD0,85%,#0B7E07,#0B7E07,#0D9209,#FFFFFF,#B3F6B2,#4DC24C,#FFB33A",
								"#D8FCD8,#43BD41,#FFFFFF,#43BD41,85%,#FFEC00,#0B7E07,#0D9209,#FFFFFF,#B3F6B2,#4DC24C,#FFB33A",
								"#D8FCD8,#259B23,#FFFFFF,#259B23,85%,#FFEC00,#0B7E07,#0D9209,#FFFFFF,#B3F6B2,#4DC24C,#FFB33A",
								"#D8FCD8,#0B7E07,#FFFFFF,#0B7E07,85%,#FFF034,#0B7E07,#0D9209,#FFFFFF,#B3F6B2,#4DC24C,#FFB33A",
								"#259B23,#FFFFFF,#259B23,#FFFFFF,85%,#FF8000,#FFFFFF,#06C500,#FFFFFF,#DDF8DC,#259B23,#FFB33A",
								"#0B7E07,#FFFFFF,#0B7E07,#FFFFFF,85%,#FF8000,#FFFFFF,#10AF0B,#FFFFFF,#DDF8DC,#259B23,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 草绿
	 * @return
	 */
	private JSONObject caolu(){
		JSONObject res = new JSONObject();
		res.put("name", "草绿");
		res.put("index", "8");
		String[] colorParam = {"#F1FEE9,#DFFACE,#6BB041,#ECFAE3,85%,#338B0F,#338B0F,#6BB041,#FFFFFF,#DFFACE,#6BB041,#FFB33A",
								"#F1FEE9,#99D474,#FFFFFF,#99D474,85%,#FFEC00,#338B0F,#6BB041,#FFFFFF,#DFFACE,#6BB041,#FFB33A",
								"#F1FEE9,#82CE54,#FFFFFF,#82CE54,85%,#FFEC00,#338B0F,#6BB041,#FFFFFF,#DFFACE,#6BB041,#FFB33A",
								"#F1FEE9,#6BB041,#FFFFFF,#6BB041,85%,#FFEC00,#338B0F,#6BB041,#FFFFFF,#DFFACE,#6BB041,#FFB33A",
								"#82CE54,#FFFFFF,#82CE54,#FFFFFF,85%,#2D7700,#FFFFFF,#49AF0B,#FFFFFF,#ECFFE0,#82CE54,#FFB33A",
								"#6BB041,#FFFFFF,#6BB041,#FFFFFF,85%,#2D7700,#FFFFFF,#399600,#FFFFFF,#ECFFE0,#82CE54,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 紫红
	 * @return
	 */
	private JSONObject zihong(){
		JSONObject res = new JSONObject();
		res.put("name", "紫红");
		res.put("index", "9");
		String[] colorParam = {"#FCF2F5,#FFE4ED,#FF4181,#FDEFF4,85%,#FF0449,#CF2050,#CF2050,#FFFFFF,#FFE4ED,#FF4181,#FF7E54",
								"#FCF2F5,#FF4181,#FFFFFF,#FF4181,85%,#FFEC00,#CF2050,#CF2050,#FFFFFF,#FFE4ED,#FF4181,#FF7E54",
								"#FCF2F5,#E91E63,#FFFFFF,#E91E63,85%,#FFEC00,#CF2050,#CF2050,#FFFFFF,#FFE4ED,#FF4181,#FF7E54",
								"#FCF2F5,#CF2050,#FFFFFF,#CF2050,85%,#FFEC00,#CF2050,#CF2050,#FFFFFF,#FFE4ED,#FF4181,#FF7E54",
								"#E91E63,#FFFFFF,#E91E63,#FFFFFF,85%,#FF8D00,#FFFFFF,#CE0038,#FFFFFF,#FFECF2,#E91E63,#FFB33A",
								"#CF2050,#FFFFFF,#CF2050,#FFFFFF,85%,#FF8D00,#FFFFFF,#A7002E,#FFFFFF,#FFECF2,#E91E63,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 红色
	 * @return
	 */
	private JSONObject hongse(){
		JSONObject res = new JSONObject();
		res.put("name", "红色");
		res.put("index", "10");
		String[] colorParam = {"#FFF1F0,#FFD1CF,#D23630,#FFDEDD,85%,#FF0808,#D23630,#D23630,#FFFFFF,#FFD1CF,#FF4181,#FF7E54",
								"#FFF1F0,#F1635E,#FFFFFF,#F1635E,85%,#FFEC00,#D23630,#D23630,#FFFFFF,#FFD1CF,#FF4181,#FF7E54",
								"#FFF1F0,#EE3D36,#FFFFFF,#EE3D36,85%,#FFEC00,#D23630,#D23630,#FFFFFF,#FFD1CF,#FF4181,#FF7E54",
								"#FFF1F0,#D23630,#FFFFFF,#D23630,85%,#FFEC00,#D23630,#D23630,#FFFFFF,#FFD1CF,#FF4181,#FF7E54",
								"#EE3D36,#FFFFFF,#EE3D36,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FFF3E3,#EE3D36,#FFB33A",
								"#D23630,#FFFFFF,#D23630,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FFF3E3,#EE3D36,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 红赤
	 * @return
	 */
	private JSONObject hongchi(){
		JSONObject res = new JSONObject();
		res.put("name", "红赤");
		res.put("index", "11");
		String[] colorParam = {"#FFF1F0,#FFB8BB,#D23630,#FFCED0,85%,#FF0808,#E33145,#E33145,#FFFFFF,#FFB8BB,#D23630,#FF7E54",
								"#FFF1F0,#F86A6C,#FFFFFF,#F86A6C,85%,#FFEC00,#E33145,#E33145,#FFFFFF,#FFB8BB,#D23630,#FF7E54",
								"#FFF1F0,#EB4449,#FFFFFF,#EB4449,85%,#FFEC00,#E33145,#E33145,#FFFFFF,#FFB8BB,#D23630,#FF7E54",
								"#FFF1F0,#E33145,#FFFFFF,#E33145,85%,#FFEC00,#E33145,#E33145,#FFFFFF,#FFB8BB,#D23630,#FF7E54",
								"#EB4449,#FFFFFF,#EB4449,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FFF3E3,#D23630,#FFD750",
								"#E33145,#FFFFFF,#E33145,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FFF3E3,#D23630,#FFD750"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 粉红
	 * @return
	 */
	private JSONObject fenhong(){
		JSONObject res = new JSONObject();
		res.put("name", "粉红");
		res.put("index", "12");
		String[] colorParam = {"#FEE7EC,#FDD7D4,#F75968,#FFE9E8,85%,#E7091E,#E33145,#F33345,#FFFFFF,#FDD7D4,#F75968,#FF7E54",
								"#FEE7EC,#FA8094,#FFFFFF,#FA8094,85%,#FFFD90,#E33145,#F33345,#FFFFFF,#FDD7D4,#F75968,#FF7E54",
								"#FEE7EC,#F75968,#FFFFFF,#F75968,85%,#FFFD90,#E33145,#F33345,#FFFFFF,#FDD7D4,#F75968,#FF7E54",
								"#FEE7EC,#F33345,#FFFFFF,#F33345,85%,#FDFB99,#E33145,#F33345,#FFFFFF,#FDD7D4,#F75968,#FF7E54",
								"#F75968,#FFFFFF,#F75968,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FFE7E5,#F75968,#FFE7A3",
								"#F33345,#FFFFFF,#F33345,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FFE7E5,#F75968,#FFE7A3"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 红梅
	 * @return
	 */
	private JSONObject hongmei(){
		JSONObject res = new JSONObject();
		res.put("name", "红梅");
		res.put("index", "13");
		String[] colorParam = {"#FDF0F3,#FBD2D2,#BE6867,#FCDEDE,85%,#C33532,#FC7B79,#FC7B79,#FFFFFF,#FBD2D2,#BE6867,#FF7E54",
								"#FDF0F3,#FBAFAE,#FFFFFF,#FFC8C7,85%,#EF5250,#FC7B79,#FC7B79,#FFFFFF,#FBD2D2,#BE6867,#FF7E54",
								"#FDF0F3,#FF9290,#FFFFFF,#FF9290,85%,#FAFE77,#FC7B79,#FC7B79,#FFFFFF,#FBD2D2,#BE6867,#FF7E54",
								"#FDF0F3,#FC7B79,#FFFFFF,#FC7B79,85%,#FAFE77,#FC7B79,#FC7B79,#FFFFFF,#FBD2D2,#BE6867,#FF7E54",
								"#FF9290,#FFFFFF,#FF9290,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FF9290,#FFECEC,#F8411D",
								"#FC7B79,#FFFFFF,#FC7B79,#FFFFFF,85%,#FF8D00,#FFFFFF,#FF8D00,#FFFFFF,#FF9290,#FFECEC,#F8411D"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 橘红
	 * @return
	 */
	private JSONObject juhong(){
		JSONObject res = new JSONObject();
		res.put("name", "橘红");
		res.put("index", "14");
		String[] colorParam = {"#FDE6D2,#FFD0A9,#FA7048,#FAD2B0,85%,#EC3600,#FA6235,#FA6235,#FFFFFF,#FFD0A9,#FA7048,#FF970B",
								"#FDE6D2,#FD8B68,#FFFFFF,#FD8B68,85%,#FFFD90,#FA6235,#FA6235,#FFFFFF,#FFD0A9,#FA7048,#FF970B",
								"#FDE6D2,#FE7A53,#FFFFFF,#FE7A53,85%,#FFFD90,#FA6235,#FA6235,#FFFFFF,#FFD0A9,#FA7048,#FF970B",
								"#FDE6D2,#FA6235,#FFFFFF,#FA6235,85%,#FFFD90,#FA6235,#FA6235,#FFFFFF,#FFD0A9,#FA7048,#FF970B",
								"#FE7A53,#FFFFFF,#FE7A53,#FFFFFF,85%,#FB2C00,#FFFFFF,#E50012,#FFFFFF,#FFDDC1,#FA7048,#A777FF",
								"#FA6235,#FFFFFF,#FA6235,#FFFFFF,85%,#FB2C00,#FFFFFF,#E50012,#FFFFFF,#FFDDC1,#FA7048,#A777FF"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 橙色
	 * @return
	 */
	private JSONObject chengse(){
		JSONObject res = new JSONObject();
		res.put("name", "橙色");
		res.put("index", "15");
		String[] colorParam = {"#FFEACC,#FFDCA9,#E07912,#FFE3BA,85%,#FC4900,#F26B05,#F26B05,#FFFFFF,#FFDCA9,#E07912,#FF970B",
								"#FFEACC,#FF9800,#FFFFFF,#FF9800,85%,#FFEC00,#F26B05,#F26B05,#FFFFFF,#FFDCA9,#E07912,#FF970B",
								"#FFEACC,#F67C01,#FFFFFF,#F67C01,85%,#FEE500,#F26B05,#F26B05,#FFFFFF,#FFDCA9,#E07912,#FF970B",
								"#FFEACC,#F26B05,#FFFFFF,#F26B05,85%,#FFEC00,#F26B05,#F26B05,#FFFFFF,#FFDCA9,#E07912,#FF970B",
								"#F67C01,#FFFFFF,#F67C01,#FFFFFF,85%,#E75100,#FFFFFF,#FF2200,#FFFFFF,#FFF2DF,#F67C01,#A777FF",
								"#F26B05,#FFFFFF,#F26B05,#FFFFFF,85%,#E04505,#FFFFFF,#FF2200,#FFFFFF,#FFF2DF,#F67C01,#A777FF"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 郁金
	 * @return
	 */
	private JSONObject yujing(){
		JSONObject res = new JSONObject();
		res.put("name", "郁金");
		res.put("index", "16");
		String[] colorParam = {"#FFFDE3,#FFF9BA,#F47F16,#FFFAC4,85%,#E72C01,#C02F00,#FFB600,#FFFFFF,#FFF9BA,#F47F16,#FF970B",
								"#FFFDE3,#FFEA00,#F47F16,#FAEB44,85%,#C05B00,#C02F00,#FFB600,#FFFFFF,#FFF9BA,#F47F16,#FF970B",
								"#FFFDE3,#FFD600,#F47F16,#FDDA24,85%,#B82407,#C02F00,#FFB600,#FFFFFF,#FFF9BA,#F47F16,#FF970B",
								"#FFFDE3,#FBC02E,#D46503,#FAC542,85%,#8D2E08,#C02F00,#FFB600,#FFFFFF,#FFF9BA,#F47F16,#FF970B",
								"#FFD600,#FFFFFF,#F47F16,#FFFFFF,85%,#C02F00,#C02F00,#E03D00,#FFFFFF,#FFFCE2,#F47F16,#F8411D",
								"#FFC32E,#FFFFFF,#D46503,#FFFFFF,85%,#9D4A00,#C02F00,#E03D00,#FFFFFF,#FFFCE2,#F47F16,#F8411D"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 黄色
	 * @return
	 */
	private JSONObject huangse(){
		JSONObject res = new JSONObject();
		res.put("name", "黄色");
		res.put("index", "17");
		String[] colorParam = {"#FFF9EF,#FAF1D5,#B17152,#FBF5E1,85%,#A44113,#DE4600,#DE4600,#FFFFFF,#FAF1D5,#B17152,#FF970B",
								"#FFF9EF,#FFE187,#B17152,#FFE596,85%,#DE4600,#DE4600,#DE4600,#FFFFFF,#FAF1D5,#B17152,#FF970B",
								"#FFF9EF,#FDDA72,#AA664F,#FFDF81,85%,#DE4600,#DE4600,#DE4600,#FFFFFF,#FAF1D5,#B17152,#FF970B",
								"#FFF9EF,#FED24F,#B17152,#FFD967,85%,#DE4600,#DE4600,#DE4600,#FFFFFF,#FAF1D5,#B17152,#FF970B",
								"#FDDA72,#FFFFFF,#AA664F,#FFFFFF,85%,#DE4600,#DE4600,#DE4600,#FFFFFF,#FAF1D5,#B17152,#F8411D",
								"#FED24F,#FFFFFF,#B17152,#FFFFFF,85%,#DE4600,#DE4600,#DE4600,#FFFFFF,#FAF1D5,#B17152,#F8411D"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 紫罗兰
	 * @return
	 */
	private JSONObject ziluolan(){
		JSONObject res = new JSONObject();
		res.put("name", "紫罗兰");
		res.put("index", "18");
		String[] colorParam = {"#F4F2FE,#E2DDFF,#755EF2,#E2DDFF,85%,#3213DF,#6C55EA,#6C55EA,#FFFFFF,#E2DDFF,#8069F3,#FFB33A",
								"#F4F2FE,#9785FA,#FFFFFF,#9785FA,85%,#FDC612,#6C55EA,#6C55EA,#FFFFFF,#E2DDFF,#8069F3,#FFB33A",
								"#F4F2FE,#8872FD,#FFFFFF,#8872FD,85%,#FDC612,#6C55EA,#6C55EA,#FFFFFF,#E2DDFF,#8069F3,#FFB33A",
								"#F4F2FE,#6C55EA,#FFFFFF,#6C55EA,85%,#FDC612,#6C55EA,#6C55EA,#FFFFFF,#E2DDFF,#8069F3,#FFB33A",
								"#8872FD,#FFFFFF,#8872FD,#FFFFFF,85%,#4E36CC,#FFFFFF,#6C55EA,#FFFFFF,#EBE8FF,#8069F3,#FFB33A",
								"#6C55EA,#FFFFFF,#6C55EA,#FFFFFF,85%,#301BA3,#FFFFFF,#4730C8,#FFFFFF,#EBE8FF,#8069F3,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 青紫
	 * @return
	 */
	private JSONObject qingzi(){
		JSONObject res = new JSONObject();
		res.put("name", "青紫");
		res.put("index", "19");
		String[] colorParam = {"#EBE9FF,#C2CAF8,#6672B2,#C2CAF8,85%,#28305E,#6E76F6,#6E76F6,#FFFFFF,#C2CAF8,#6672B2,#FFB33A",
								"#EBE9FF,#A4B1FB,#FFFFFF,#A4B1FB,85%,#FDE312,#6E76F6,#6E76F6,#FFFFFF,#C2CAF8,#6672B2,#FFB33A",
								"#EBE9FF,#8598FF,#FFFFFF,#8598FF,85%,#FDE312,#6E76F6,#6E76F6,#FFFFFF,#C2CAF8,#6672B2,#FFB33A",
								"#EBE9FF,#6E76F6,#FFFFFF,#6E76F6,85%,#FDE312,#6E76F6,#6E76F6,#FFFFFF,#C2CAF8,#6672B2,#FFB33A",
								"#8598FF,#FFFFFF,#8598FF,#FFFFFF,85%,#304BDA,#FFFFFF,#444CD4,#FFFFFF,#E4E8FF,#6672B2,#FFB33A",
								"#6E76F6,#FFFFFF,#6E76F6,#FFFFFF,85%,#333CCE,#FFFFFF,#444CD4,#FFFFFF,#E4E8FF,#6672B2,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 紫色
	 * @return
	 */
	private JSONObject zise(){
		JSONObject res = new JSONObject();
		res.put("name", "紫色");
		res.put("index", "20");
		String[] colorParam = {"#FAECFF,#EFCFFA,#A86EBE,#EFCFFA,85%,#7C189F,#C666E8,#C666E8,#FFFFFF,#EFCFFA,#AE77C3,#FFB33A",
								"#FAECFF,#DBADEB,#FFFFFF,#DBADEB,85%,#FAFE77,#C666E8,#C666E8,#FFFFFF,#EFCFFA,#AE77C3,#FFB33A",
								"#FAECFF,#CD84E7,#FFFFFF,#CD84E7,85%,#FAFE77,#C666E8,#C666E8,#FFFFFF,#EFCFFA,#AE77C3,#FFB33A",
								"#FAECFF,#C666E8,#FFFFFF,#C666E8,85%,#FAFE77,#C666E8,#C666E8,#FFFFFF,#EFCFFA,#AE77C3,#FFB33A",
								"#CD84E7,#FFFFFF,#CD84E7,#FFFFFF,85%,#BA2ABD,#FFFFFF,#B655D9,#FFFFFF,#FBEFFF,#AE77C3,#FFB33A",
								"#C666E8,#FFFFFF,#C666E8,#FFFFFF,85%,#9E16CE,#FFFFFF,#A73CCD,#FFFFFF,#FBEFFF,#AE77C3,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 牡丹
	 * @return
	 */
	private JSONObject modan(){
		JSONObject res = new JSONObject();
		res.put("name", "牡丹");
		res.put("index", "21");
		String[] colorParam = {"#FCDEF0,#FFC7E8,#F365B9,#FED9EF,85%,#E81751,#E165AE,#E165AE,#FFFFFF,#FFC7E8,#F365B9,#FF7E54",
								"#FCDEF0,#F596CF,#FFFFFF,#F596CF,85%,#FFEC00,#E165AE,#E165AE,#FFFFFF,#FFC7E8,#F365B9,#FF7E54",
								"#FCDEF0,#F387C4,#FFFFFF,#F387C4,85%,#FFEC00,#E165AE,#E165AE,#FFFFFF,#FFC7E8,#F365B9,#FF7E54",
								"#FCDEF0,#E165AE,#FFFFFF,#E165AE,85%,#FFEC00,#E165AE,#E165AE,#FFFFFF,#FFC7E8,#F365B9,#FF7E54",
								"#F387C4,#FFFFFF,#EA86BF,#FFFFFF,85%,#B12D79,#FFFFFF,#E3369C,#FFFFFF,#FFE4F4,#EA86BF,#A777FF",
								"#E165AE,#FFFFFF,#D670AC,#FFFFFF,85%,#AB2A76,#FFFFFF,#E22393,#FFFFFF,#FFE4F4,#EA86BF,#A777FF"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	/**
	 * 咖啡
	 * @return
	 */
	private JSONObject kafei(){
		JSONObject res = new JSONObject();
		res.put("name", "咖啡");
		res.put("index", "22");
		String[] colorParam = {"#FFD9D9,#F4B0A4,#892E1D,#F4B0A4,85%,#591212,#591212,#8E1E1C,#FFFFFF,#F4B0A4,#892E1D,#FF970B",
								"#FFD9D9,#BF2626,#FFFFFF,#BF2626,85%,#FFEC00,#591212,#8E1E1C,#FFFFFF,#F4B0A4,#892E1D,#FF970B",
								"#FFD9D9,#8E1E1C,#FFFFFF,#8E1E1C,85%,#FFEC00,#591212,#8E1E1C,#FFFFFF,#F4B0A4,#892E1D,#FF970B",
								"#FFD9D9,#591212,#FFFFFF,#591212,85%,#FFEC00,#591212,#8E1E1C,#FFFFFF,#F4B0A4,#892E1D,#FF970B",
								"#CE5828,#FFFFFF,#944233,#FFFFFF,85%,#591212,#FFFFFF,#8E1E1C,#FFFFFF,#F4C399,#892E1D,#FF970B",
								"#bf2626,#FFFFFF,#752719,#FFFFFF,85%,#3f0d0d,#FFFFFF,#721717,#FFFFFF,#F4C399,#892E1D,#FFB33A"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	
	/**
	 * 赭石
	 * @return
	 */
	private JSONObject zheshi(){
		JSONObject res = new JSONObject();
		res.put("name", "赭石");
		res.put("index", "23");
		String[] colorParam = {"#FFEDE6,#FFB699,#892E1D,#FFB699,85%,#591212,#723117,#723117,#FFFFFF,#FFC8B3,#8C3C1C,#FF970B",
								"#FFEDE6,#D85C2B,#FFFFFF,#D85C2B,85%,#FFEC00,#723117,#723117,#FFFFFF,#FFC8B3,#8C3C1C,#FF970B",
								"#FFEDE6,#A54721,#FFFFFF,#A54721,85%,#FFEC00,#723117,#723117,#FFFFFF,#FFC8B3,#8C3C1C,#FF970B",
								"#FFEDE6,#723117,#FFFFFF,#723117,85%,#FFEC00,#723117,#723117,#FFFFFF,#FFC8B3,#8C3C1C,#FF970B",
								"#D85C2B,#FFFFFF,#892E1D,#FFFFFF,85%,#591212,#FFFFFF,#723117,#FFFFFF,#FFD9CA,#8C3C1C,#F3CC6C",
								"#a54721,#FFFFFF,#752719,#FFFFFF,85%,#3F0D0D,#FFFFFF,#5A2611,#FFFFFF,#FFD9CA,#8C3C1C,#FF970B"};
		res.put("list", getColorDetail(colorParam));
		return res;
	}
	
	private List<JSONObject> getColorDetail(String[] colorParam){
		
		List<JSONObject> list = new ArrayList<JSONObject>();
		for(int i = 0; i < colorParam.length; i++){
			String[] single = colorParam[i].split(",");
			JSONObject one = new JSONObject();
			one.put("pgColor", single[0]);
			one.put("tabBgColor", single[1]);
			one.put("tabTxtColor", single[2]);
			one.put("tabHowerBgColor", single[3]);
			one.put("tabHower", single[4]);
			one.put("tabHhTxtColor",single[5]);
			one.put("fTitleColor", single[6]);
			one.put("mTabHowerBgColor", single[7]);
			one.put("mTabHhTxtColor",single[8]);
			one.put("mTabBgColor", single[9]);
			one.put("mTabTxtColor", single[10]);
			one.put("mCouponColor", single[11]);
			one.put("mCouponLeftColor", "#"+ colorCompute(single[11].substring(1,3), -20)+ colorCompute(single[11].substring(3,5), -80)+ colorCompute(single[11].substring(5,7), 30));
			list.add(one);
		}
		return list;
		
	}

	private String colorCompute(String orinal, int step){
		int old = Integer.parseInt(orinal,16);
		int newInt = old + step;
		if(newInt > 255){
			newInt = 255;
		}
		if(newInt < 0){
			newInt = 0;
		}
		return Integer.toHexString(newInt).toUpperCase();

	}
	
	public static JSONArray getPageColor(){
		return color;
	}
	
}
