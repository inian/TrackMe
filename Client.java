import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import javax.print.attribute.standard.Finishings;

public class Client {
	/**
	 * @param args
	 * @throws IOException
	 */

	private static String host_url;
	private static String protocol_url;
	private static String path;
	int count;

	public static void main(String[] args) throws IOException {
		// String given_url = "http://www.unc.edu/~bhamidi/personal/naruto.jpg";
		// String given_url =
		// "http://upload.wikimedia.org/wikipedia/en/thumb/0/0b/Shipuden_soundtrack.jpg/220px-Shipuden_soundtrack.jpg";
		String given_url = "http://www.comp.nus.edu.sg/~amulya/p1/d0.html";
		URL url = new URL(given_url);
		String host = url.getHost();
		host_url = host;
		protocol_url = url.getProtocol();
		path = url.getPath();
		System.out.println(path);
		// path = path.substring(0, path.lastIndexOf("/"));
		// String path = url.getFile();
		// create a folder with the hostname
		File file = new File(host);
		if (!file.exists()) {
			file.mkdir();
		}
		String fileName = getFileName(url);
		String webpage = downloadWebPage(host, path);

		String path1 = host + "/";
		System.out.println(webpage);
		saveFile(webpage, fileName);
		getLink(0, webpage);
	}

	public static void getLink(int start, String content) {
		content = content.trim().replaceAll(" +", " "); // now there is space
		int pos = content.indexOf("=", start);
		int pos_intial = pos;
		if (pos >= 0) {
			if (content.charAt(pos - 1) == ' ') {
				pos--;
			}
			if (content.substring(pos - 4, pos).equals("href")
					|| content.substring(pos - 3, pos).equals("src")) {
				int start_string = content.substring(pos).indexOf('"');
				int end_string = content.substring(pos).indexOf('"',
						start_string + 1);
				String extracted_url = content.substring(start_string
						+ content.substring(0, pos).length() + 1, end_string
						+ content.substring(0, pos).length());

				String extracted_url_original = extracted_url;
				if (!extracted_url.startsWith(protocol_url)) {
					String directory = path.substring(0, path.lastIndexOf("/"));
					extracted_url = protocol_url + "://" + host_url + directory
							+ "/" + extracted_url;
				}
				System.out.println(extracted_url);
				URL url = null;
				try {
					url = new URL(extracted_url);
					path = url.getPath();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				try {
					// only download if it is from the same server
					if (url.getHost().equals(host_url)) {
						System.out.println("Downlaoding " + url.getPath());
						String webpage = downloadWebPage(url.getHost(),
								url.getPath());
						String fileName = getFileName(url);
						// webpage = webpage.replace(extracted_url_original,
						// fileName);
						// System.out.println(extracted_url_original + " with "
						// + fileName);
						saveFile(webpage, fileName);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// getLink(0, content.substring(pos + 1));
		}
		if (content.substring(pos_intial + 1).length() > 0) {
			getLink(0, content.substring(pos_intial + 1));
		}
	}

	private static String getFileName(URL url) {
		String[] parts = url.toString().split("/");
		System.out.println(parts[parts.length - 1]);
		String fileName = parts[parts.length - 1];
		System.out.println("File name = " + fileName);
		// File file = new File(url.toString());
		File file = new File(host_url + "/" + fileName);
		if (file.exists()) {
			String fileNameWithoutExtension = fileName.substring(0,
					fileName.lastIndexOf("."));
			String extension = fileName.substring(fileName.lastIndexOf("."),
					fileName.length());
			fileNameWithoutExtension += "1";
			System.out.println(fileNameWithoutExtension);
			fileName = fileNameWithoutExtension + extension;
		}
		return fileName;
	}

	/*
	 * This function sets up the socket and communicates with the server to
	 * download a particular web page.
	 */
	public static String downloadWebPage(String hostname, String path)
			throws IOException {

		// setup the communication socket for port 80.
		Socket socket = new Socket(hostname, 80);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		// write the GET request to the out stream.
		OutputStream out = socket.getOutputStream();
		PrintWriter outw = new PrintWriter(out, false);
		outw.print("GET /" + path + " HTTP/1.0\r\n");
		outw.print("Host:" + hostname + "\r\n");
		// outw.print("Accept: text/plain, text/html, text/*, image/* 	\r\n");
		outw.print("Accept: */*	\r\n");
		outw.print("\r\n");
		outw.flush();
		String line;
		String result = "";
		// reading the response from the server and splitting into header and
		// HTML
		int c = 0;
		String temp = "";

		while ((line = inFromServer.readLine()) != null) {
			// if first blank line is encountered, the header is over.
			if (line.equalsIgnoreCase("")) {
				while ((c = inFromServer.read()) != -1) {
					// System.out.print((char) c);
					temp += (char) c;
				}
				socket.close();
				return temp;
			}
		}
		socket.close();
		return result;
	}

	public static boolean saveFile(String content, String fileName) {
		// path = path.substring(0, path.lastIndexOf("/")) + "/";
		// System.out.println("path = "+path);
		File f = new File(host_url + "/" + fileName);
		try {
			PrintWriter writer = new PrintWriter(f);
			writer.print(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
