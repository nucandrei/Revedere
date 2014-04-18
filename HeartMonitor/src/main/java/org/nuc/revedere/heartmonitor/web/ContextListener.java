package org.nuc.revedere.heartmonitor.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.nuc.revedere.heartmonitor.HeartMonitor;

@WebListener
public class ContextListener implements ServletContextListener{

    public void contextInitialized(ServletContextEvent args) {
        HeartMonitor.getInstance();
    }
    
    public void contextDestroyed(ServletContextEvent args) {
       
    }



}
