package org.sp.app0721.network.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

//서버에 접속하는 각각의 클라이언트마다 1:1 대응하는 대화전용 쓰레드 정의
public class MessageThread extends Thread{
	GUIServer guiServer;
	Socket socket;
	BufferedReader buffr; //버퍼 처리된 문자 기반 입력 스트림
	BufferedWriter buffw; //버퍼 처리된 문자 기반 출력 스트림
	boolean loopFlag=true;
	
	public MessageThread(GUIServer guiServer, Socket socket) {
		this.guiServer=guiServer;
		this.socket=socket;
		
		try {
			buffr=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			buffw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//듣기
	public void listen() {
		String msg=null;
		try {
			msg=buffr.readLine(); //듣기
			
			//접속한 모든 사용자마다 대응되는 메시지 쓰레드 객체의 sendMsg() 메서드 호출.
			for(int i=0; i<guiServer.vec.size(); i++) {
				MessageThread mt=guiServer.vec.get(i);
				mt.sendMsg(msg); //다시 보내기				
			}
			
			//로그 남기기
			guiServer.area.append(msg+"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//말하기
	public void sendMsg(String msg) {
		try {
			buffw.write(msg+"\n"); //출력한 문장 단위를 구분짓기 위해 \n
			buffw.flush(); //반드시 출력 스트림은 출력 후 비우기
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("접속사 퇴장. 읽어들일 메시지 없음");
			loopFlag=false;
			
			guiServer.vec.remove(this);
			guiServer.area.append("현재 접속자 수 "+guiServer.vec.size()+"\n");
		
			
		}
	}
	
	//쓰레드는 run() 메서드의 닫는 브레이스를 만나면 소멸되므로, 죽이지 않으려면 무한 루프로 처리해야 한다. -> 메인 실행부가 아닌 readLinge() 메서드가 대기상태에 빠지기 때문에 안정적임
	public void run() {
		while(loopFlag) {
			listen();
		}
	}
}
