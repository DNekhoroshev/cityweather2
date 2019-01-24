package ru.sberbank.example.services;

import java.io.IOException;
import java.io.PrintWriter;

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
		// TODO Auto-generated method stub
	}

	/**
	 * @see Servlet#service(ServletRequest request, ServletResponse response)
	 */
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		PrintWriter out = response.getWriter();
		
		if("GET".equals(httpRequest.getMethod())){
			try {			
				
				String cityName = httpRequest.getParameter("cityName");		
	            				
		        OpenWeatherMapClient owmc = new OpenWeatherMapClient(APP_ID);
		        
		        JSONObject servletResponseJson = new JSONObject();
				servletResponseJson.put("temperature", owmc.getTempByCity(cityName));		
				
				httpResponse.setContentType("application/json");
				out.print(servletResponseJson.toString());
				
			}catch(Exception e) {
				LOGGER.error("Connectivity operation failed", e);
				httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}finally {
				out.flush();
			}			
		}else {
			httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not allowed!");		
		}
	}

	
	
	
	  
	
}
