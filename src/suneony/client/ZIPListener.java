package suneony.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;

public class ZIPListener {
	private Properties config = null;
	private Socket client = null;
	private InputStream socketInputStream = null;
	private FileOutputStream fileOutputStream = null;
	public ZIPListener() {
		config = new Properties();
		InputStream configStream = null;
		try {
			configStream = new FileInputStream("config.properties");
			config.load(configStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// close the stream
				configStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void listener() {
		while (true) {
			try {
				System.out.println("begin");
				client = new Socket(config.getProperty("SERVER_IP"),
						Integer.valueOf(config.getProperty("SERVER_PORT")));
				socketInputStream = client.getInputStream();
				// 接收信号,返回新文件名或no
				byte[] buffIn = new byte[1024];
				int lenIn = socketInputStream.read(buffIn);
				System.out.println(lenIn);
				String signal = new String(buffIn, 0, lenIn);
				System.out.println(signal);
				if (signal.equals("NO")) {
					// 如果没有新文件，等待10分钟
					Thread.currentThread();
					Thread.sleep(1200000);
				} else {
					String[] info = signal.split("/");
					String yearString = info[0];
					String monthString = info[1];
					String dayString = info[2];
					String zipName = info[3];
					String absoluteZipPath = config.getProperty("SAVE_PATH") + "/" + yearString + "/" + monthString
							+ "/" + dayString + "/";
					File zipDir = new File(absoluteZipPath);
					if (!zipDir.exists())
						zipDir.mkdirs();
					fileOutputStream = new FileOutputStream(new File(absoluteZipPath + zipName));
					byte[] fileBuff = new byte[1024 * 1024];
					int len = 0;
					while (true) {
						len = socketInputStream.read(fileBuff);
						if (-1 != len) {
							fileOutputStream.write(fileBuff, 0, len);
						} else {
							break;
						}
					}
					if (fileOutputStream != null) {
						try {
							fileOutputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (socketInputStream != null) {
					try {
						socketInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (client != null) {
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public static void main(String[] args) {
		ZIPListener zipListener = new ZIPListener();
		zipListener.listener();
	}
}
