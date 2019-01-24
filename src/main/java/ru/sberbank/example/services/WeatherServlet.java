package ru.sberbank.example.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.NamingException;
import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.example.services.exception.DestinationNotFoundException;

/**
 * Servlet implementation class WeatherServlet
 */
@WebServlet(
		urlPatterns = { "/WeatherServlet" } 
		)
public class WeatherServlet extends GenericServlet {
	private static final long serialVersionUID = 1L;
	private final String APP_ID = "8f56ccda96cd0d0eebfc792dbf952290";
	private static final Logger LOGGER = LoggerFactory.getLogger(WeatherServlet.class);	
	
	private OpenWeatherMapClient client;
	
	/**
     * @see GenericServlet#GenericServlet()
     */
    public WeatherServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		 try {
			this.client = new OpenWeatherMapClient(APP_ID);
		} catch (Exception e) {
			throw new ServletException(e);
		} 
	}

	/**
	 * @see Servlet#service(ServletRequest request, ServletResponse response)
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		PrintWriter out = response.getWriter();
		
		if("GET".equals(httpRequest.getMethod())){
			
			if(client==null){
				httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service was not correctly initialized");
				return;
			}
			
			String cityName = httpRequest.getParameter("cityName");
			
			if((cityName==null)||(cityName.isEmpty())){
				httpResponse.setContentType("text/plain");
				httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty city name is not allowed");
				return;
			}
			
			try {			       
		        
		        JSONObject servletResponseJson = new JSONObject();
				servletResponseJson.put("temperature", client.getTempByCity(cityName));		
				
				httpResponse.setContentType("application/json");
				out.print(servletResponseJson.toString());
				
			}catch(Exception e) {
				LOGGER.error("Connectivity operation failed", e);
				httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}finally {
				out.flush();
			}			
		}else {
			httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not allowed");		
		}
	}

	
	
	
	  
	
}
