package com.nemchina.explorer.fetch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.nemchina.explorer.http.SuperNode;

/** 
 * @Description: Fetch SuperNode Info
 * @author lu
 * @date 2016年8月22日
 */ 
public class FetchSuperNode extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public void init() throws ServletException {
		
		ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
		pool.scheduleWithFixedDelay(new Runnable(){
			public void run() {
				SuperNode.fetchSuperNodes();
			};
		}, 0, 5 * 60 * 1000, TimeUnit.MILLISECONDS);
	}
}
