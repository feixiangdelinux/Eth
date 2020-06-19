import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Eth {
	private static ArrayList<Double> allPrice = new ArrayList<Double>();
	private static Gson gson = new Gson();
	private static Session session;
	private static String filePash;
	private static BtcProfileBean mBtcProfile;

	public static void main(String[] args) {
		// 配置参数
		Properties prop = new Properties();
		// 发件人的邮箱的SMTP 服务器地址（不同的邮箱，服务器地址不同，如139和qq的邮箱服务器地址不同）
		prop.setProperty("mail.host", "smtp.qq.com");
		// 使用的协议（JavaMail规范要求）
		prop.setProperty("mail.transport.protocol", "smtp");
		// 需要请求认证
		prop.setProperty("mail.smtp.auth", "true");
		// 使用JavaMail发送邮件的5个步骤
		// 1、创建session
		session = Session.getInstance(prop);
		// 1.创建Scanner类的对象
		Scanner s = new Scanner(System.in);

		// System.out.println("先输入pe文件路径比如 /home/ccg/文档/BtcPrice.txt 或
		// C:/BtcPrice.txt");
		System.out.println("先配置好配置文件，然后输入配置文件路径比如   /home/ccg/文档/BtcProfile.txt  或  E:/BtcProfile.txt");
		filePash = s.next();

		System.out.println("开始运行脚本：(输入数字2停止脚本并保存数据)");
		// 读取配置文件
		File file = new File(filePash);
		boolean isHave = file.exists();
		String jsonString = "";
		if (isHave) {
			jsonString = txt2String(file);
		}
		if (!jsonString.isEmpty()) {
			mBtcProfile = gson.fromJson(jsonString, BtcProfileBean.class);
		}
		// 导入历史价格
		File fileTwo = new File(mBtcProfile.getFilePash());
		String jsonStringTwo = "";
		if (fileTwo.exists()) {
			jsonStringTwo = txt2String(fileTwo);
		}
		if (!jsonStringTwo.isEmpty()) {
			allPrice.clear();
			ArrayList timpList = gson.fromJson(jsonStringTwo, new TypeToken<ArrayList<Double>>() {
			}.getType());
			allPrice.addAll(timpList);
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
//				getPrice();
				getPriceTwo();
			}
		}, 0, 900000);
		while (true) {
			int countTwo = s.nextInt();
			if (countTwo != 2) {
				System.out.println("输入数字2停止脚本并保存数据");
			} else {
				System.out.println("2停止脚本保存数据");
				// 把总数据保存到/home/ccg/文档/BtcPrice.txt中
				File fileThree = new File(mBtcProfile.getFilePash());
				if (!fileThree.exists()) {
					createFile(fileThree);
				}
				writeTxtFile(gson.toJson(allPrice), fileThree);
				break;
			}

		}
		System.out.println("脚本执行结束");

		
	}

	/**
	 * 把字符串写入文件
	 * 
	 * @param content
	 * @param fileName
	 * @return
	 */
	public static boolean writeTxtFile(String content, File fileName) {
		RandomAccessFile mm = null;
		boolean flag = false;
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(fileName);
			o.write(content.getBytes("UTF-8"));
			o.close();
			flag = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (mm != null) {
				try {
					mm.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

	/**
	 * 获取价格
	 */
	private static void getPriceTwo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer stringBuffer = new StringBuffer();
				try {
					URL url = new URL("https://www.usd-cny.com/btc/b.js");
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setConnectTimeout(5 * 1000);// 链接超时
					urlConnection.setReadTimeout(5 * 1000);// 返回数据超时
					urlConnection.setRequestProperty("User-Agent",
							"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
					// getResponseCode (1.200请求成功 2.404请求失败)
					if (urlConnection.getResponseCode() == 200) {
						// 获得读取流写入
						InputStream inputStream = urlConnection.getInputStream();
						byte[] bytes = new byte[1024];
						int len = 0;
						while ((len = inputStream.read(bytes)) != -1) {
							stringBuffer.append(new String(bytes, 0, len, "UTF-8"));
						}
						String[] as = stringBuffer.toString().split(",");
						String str =as[as.length-4];
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
						System.out.println(
								df.format(new Date()) + "    " + str);
						saveCurrentPrice(str);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	/**
	 * 获取价格
	 */
	private static void getPrice() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer stringBuffer = new StringBuffer();
				try {
					URL url = new URL("https://sochain.com/api/v2/get_price/ETH/USD");
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setConnectTimeout(5 * 1000);// 链接超时
					urlConnection.setReadTimeout(5 * 1000);// 返回数据超时
					urlConnection.setRequestProperty("User-Agent",
							"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
					// getResponseCode (1.200请求成功 2.404请求失败)
					if (urlConnection.getResponseCode() == 200) {
						// 获得读取流写入
						InputStream inputStream = urlConnection.getInputStream();
						byte[] bytes = new byte[1024];
						int len = 0;
						while ((len = inputStream.read(bytes)) != -1) {
							stringBuffer.append(new String(bytes, 0, len));
						}
						RoomBean mRoomBean = gson.fromJson(stringBuffer.toString(), RoomBean.class);
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
						System.out.println(
								df.format(new Date()) + "    " + mRoomBean.getData().getPrices().get(0).getPrice());
						saveCurrentPrice(mRoomBean.getData().getPrices().get(0).getPrice());
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	/**
	 * 根据当前的价格判断是否发送通知
	 *
	 * @param price
	 */
	private static void saveCurrentPrice(String price) {
		String formatPrice = price;
		int aa = price.lastIndexOf(".");
		if (price.length() > aa + 3) {
			formatPrice = price.substring(0, aa + 3);
		}
		double currentPrice = Double.valueOf(formatPrice);
		int siz = allPrice.size();
		if (siz == 0) {
			allPrice.add(currentPrice);
		} else {
			remindOne(currentPrice, mBtcProfile.getOne());
			remindTwo(currentPrice, mBtcProfile.getTwo());
			remindThree(currentPrice, allPrice.get(allPrice.size() - 1), mBtcProfile.getThree());
			remindFour(currentPrice, allPrice.get(allPrice.size() - 1), mBtcProfile.getFour());
			remindFive(currentPrice, allPrice, mBtcProfile.isFive());
			remindSix(currentPrice, allPrice, mBtcProfile.isSix());
			// 把新数据添加到总数据中
			if (allPrice.size() >= 100) {
				allPrice.remove(0);
			}
			allPrice.add(currentPrice);
		}
	}

	/**
	 * 涨到多少提醒
	 * 
	 * @param currentPrice
	 * @param highPrice
	 */
	private static void remindOne(double currentPrice, double highPrice) {
		if (highPrice != 0.0 && currentPrice >= highPrice) {
			double maxPrice = (double) Collections.max(allPrice);
			double minPrice = (double) Collections.min(allPrice);
			double averagePrice = allPrice.stream().mapToDouble((x) -> x).average().getAsDouble();
			sendMsg(session, "涨到" + highPrice + "提醒",
					"当前价格:" + currentPrice + "  历史最高:" + maxPrice + "  历史最低:" + minPrice + "  平均价格:" + averagePrice);
		}
	}

	/**
	 * 跌到多少提醒
	 * 
	 * @param currentPrice
	 * @param highPrice
	 */
	private static void remindTwo(double currentPrice, double lowPrice) {
		if (lowPrice != 0.0 && currentPrice <= lowPrice) {
			double maxPrice = (double) Collections.max(allPrice);
			double minPrice = (double) Collections.min(allPrice);
			double averagePrice = allPrice.stream().mapToDouble((x) -> x).average().getAsDouble();
			sendMsg(session, "跌到" + lowPrice + "提醒",
					"当前价格:" + currentPrice + "  历史最高:" + maxPrice + "  历史最低:" + minPrice + "  平均价格:" + averagePrice);
		}
	}

	/**
	 * 比15分钟前高多少提醒
	 * 
	 * @param currentPrice
	 * @param lastPrice
	 * @param dValue
	 */
	private static void remindThree(double currentPrice, double lastPrice, double dValue) {
		if (dValue != 0.0 && (currentPrice - lastPrice) > dValue) {
			double maxPrice = (double) Collections.max(allPrice);
			double minPrice = (double) Collections.min(allPrice);
			double averagePrice = allPrice.stream().mapToDouble((x) -> x).average().getAsDouble();
			sendMsg(session, "比15分钟前高" + dValue + "提醒",
					"当前价格:" + currentPrice + "  历史最高:" + maxPrice + "  历史最低:" + minPrice + "  平均价格:" + averagePrice);
		}
	}

	/**
	 * 比15分钟前低多少提醒
	 * 
	 * @param currentPrice
	 * @param lastPrice
	 * @param dValue
	 */
	private static void remindFour(double currentPrice, double lastPrice, double dValue) {
		if (dValue != 0.0 && (lastPrice - currentPrice) > dValue) {
			double maxPrice = (double) Collections.max(allPrice);
			double minPrice = (double) Collections.min(allPrice);
			double averagePrice = allPrice.stream().mapToDouble((x) -> x).average().getAsDouble();
			sendMsg(session, "比15分钟前低" + dValue + "提醒",
					"当前价格:" + currentPrice + "  历史最高:" + maxPrice + "  历史最低:" + minPrice + "  平均价格:" + averagePrice);
		}
	}

	/**
	 * 和以前的100次价格中最高的价格进行比较如果比最高的价格还高就提醒
	 * 
	 * @param currentPrice
	 * @param allPrice
	 * @param isCheck
	 */
	private static void remindFive(double currentPrice, ArrayList<Double> allPrice, boolean isCheck) {
		double maxPrice = (double) Collections.max(allPrice);
		if (isCheck && currentPrice >= maxPrice) {
			double minPrice = (double) Collections.min(allPrice);
			double averagePrice = allPrice.stream().mapToDouble((x) -> x).average().getAsDouble();
			sendMsg(session, "当前价格和前100次比已经达到最高的价格",
					"当前价格:" + currentPrice + "  历史最高:" + maxPrice + "  历史最低:" + minPrice + "  平均价格:" + averagePrice);
		}
	}

	/**
	 * 和以前的100次价格中最低的价格进行比较如果比最低的价格还低就提醒
	 * 
	 * @param currentPrice
	 * @param allPrice
	 * @param isCheck
	 */
	private static void remindSix(double currentPrice, ArrayList<Double> allPrice, boolean isCheck) {
		double minPrice = (double) Collections.min(allPrice);
		if (isCheck && currentPrice <= minPrice) {
			double maxPrice = (double) Collections.max(allPrice);
			double averagePrice = allPrice.stream().mapToDouble((x) -> x).average().getAsDouble();
			sendMsg(session, "当前价格和前100次比已经达到最低的价格",
					"当前价格:" + currentPrice + "  历史最高:" + maxPrice + "  历史最低:" + minPrice + "  平均价格:" + averagePrice);
		}
	}

	/**
	 * 发送消息
	 *
	 * @param allPrice
	 * @param currentPrice
	 * @param tianshu
	 */
	public static void sendInfo(List<Double> allPrice, double currentPrice, int tianshu) {
		if (tianshu > 0) {
			if (currentPrice <= Collections.min(allPrice)) {
				sendMsg(session, "" + tianshu + "天内最低价 ", "当前价格是：" + currentPrice);
				return;
			}
			sendInfo(allPrice.subList(100, allPrice.size()), currentPrice, tianshu - 1);
		} else {
			return;
		}
	}

	/**
	 * 创建文件
	 *
	 * @param fileName
	 * @return
	 */
	public static boolean createFile(File fileName) {
		boolean flag = false;
		try {
			if (!fileName.exists()) {
				fileName.createNewFile();
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 读取文件中的内容
	 * 
	 * @param file
	 * @return
	 */
	public static String txt2String(File file) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
			String s = null;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				result.append(System.lineSeparator() + s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 发送电子邮件
	 * 
	 * @param session
	 * @param title
	 * @param content
	 */
	public static void sendMsg(Session session, String title, String content) {
		System.out.println("推送通知：   " + title + "  " + content);
		Transport ts = null;
		try {
			// 2、通过session得到transport对象
			ts = session.getTransport();
			// 3、使用邮箱的用户名和密码连接邮件服务器（不同类型的邮箱不一样，网易邮箱输入的是用户名和密码，这里我用的qq邮箱，输入的是邮箱用户名和smtp授权码，smtp授权码可登陆邮箱，进入设置启动smtp服务后获取）
			// 发送邮件时，发件人需要提交邮箱的用户名和密码给smtp服务器，用户名和密码都通过验证之后才能够正常发送邮件给收件人。
			ts.connect("smtp.qq.com", "957493412@qq.com", "pdtsanllybzwbdgg");
			// 4、创建邮件
			Message message = createSimpleMail(session, title, content, "1421760774@qq.com");
			// 5、发送邮件
			ts.sendMessage(message, message.getAllRecipients());
			Message messageTwo = createSimpleMail(session, title, content, "linux1997@qq.com");
			ts.sendMessage(messageTwo, messageTwo.getAllRecipients());
			Message messageThree = createSimpleMail(session, title, content, "linux1998@qq.com");
			ts.sendMessage(messageThree, messageThree.getAllRecipients());
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭transport对象
				ts.close();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建一封只包含文本的邮件
	 *
	 * @param session
	 * @return
	 * @throws MessagingException
	 */
	public static MimeMessage createSimpleMail(Session session, String title, String content, String addressee)
			throws MessagingException {
		// 创建邮件对象
		MimeMessage message = new MimeMessage(session);
		// 指明发件人
		message.setFrom(new InternetAddress("957493412@qq.com"));
		// 指明收件人
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(addressee));
		// 邮件的标题
		message.setSubject(title);
		// 邮件的文本内容
		message.setContent(content, "text/html;charset=UTF-8");
		return message;
	}

	static class BtcProfileBean {
		/**
		 * 收件人
		 */
		private String addressee;
		/**
		 * 读写文件的位置
		 */
		private String filePash;
		/**
		 * 涨到多少提醒
		 */
		private double one = 0.0;
		/**
		 * 跌到多少提醒
		 */
		private double two = 0.0;
		/**
		 * 比15分钟前高多少提醒
		 */
		private double three = 0.0;
		/**
		 * 比15分钟前低多少提醒
		 */
		private double four = 0.0;

		/**
		 * 和以前的100次价格中最高的价格进行比较如果比最高的价格还高就提醒
		 */
		private boolean five;
		/**
		 * 和以前的100次价格中最低的价格进行比较如果比最低的价格还低就提醒
		 */
		private boolean six;

		public String getFilePash() {
			return filePash;
		}

		public void setFilePash(String filePash) {
			this.filePash = filePash;
		}

		public double getOne() {
			return one;
		}

		public void setOne(double one) {
			this.one = one;
		}

		public double getTwo() {
			return two;
		}

		public void setTwo(double two) {
			this.two = two;
		}

		public double getThree() {
			return three;
		}

		public void setThree(double three) {
			this.three = three;
		}

		public double getFour() {
			return four;
		}

		public void setFour(double four) {
			this.four = four;
		}

		public boolean isFive() {
			return five;
		}

		public void setFive(boolean five) {
			this.five = five;
		}

		public boolean isSix() {
			return six;
		}

		public void setSix(boolean six) {
			this.six = six;
		}

	}

	class RoomBean {

		private String status;
		private DataBean data;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public DataBean getData() {
			return data;
		}

		public void setData(DataBean data) {
			this.data = data;
		}

		class DataBean {
			private String network;
			private List<PricesBean> prices;

			public String getNetwork() {
				return network;
			}

			public void setNetwork(String network) {
				this.network = network;
			}

			public List<PricesBean> getPrices() {
				return prices;
			}

			public void setPrices(List<PricesBean> prices) {
				this.prices = prices;
			}

			class PricesBean {
				private String price;
				private String price_base;
				private String exchange;
				private int time;

				public String getPrice() {
					return price;
				}

				public void setPrice(String price) {
					this.price = price;
				}

				public String getPrice_base() {
					return price_base;
				}

				public void setPrice_base(String price_base) {
					this.price_base = price_base;
				}

				public String getExchange() {
					return exchange;
				}

				public void setExchange(String exchange) {
					this.exchange = exchange;
				}

				public int getTime() {
					return time;
				}

				public void setTime(int time) {
					this.time = time;
				}

				@Override
				public String toString() {
					return "PricesBean{" + "price='" + price + '\'' + ", price_base='" + price_base + '\''
							+ ", exchange='" + exchange + '\'' + ", time=" + time + '}';
				}
			}
		}
	}
}
