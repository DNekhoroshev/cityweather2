package ru.sberbank.example.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

/**
 * Servlet implementation class WeatherServlet
 */
@WebServlet(
		urlPatterns = { "/WeatherServlet" }, 
		initParams = { 
				@WebInitParam(name = "AppId", value = "123", description = "Application ID for weather service API")
		})
public class WeatherServlet extends GenericServlet {
	private static final long serialVersionUID = 1L;
	private final String APP_ID = "8f56ccda96cd0d0eebfc792dbf952290";
	private static final String ON_PREMISE_PROXY = "OnPremise";
	private static final String destinationName = "OpenWeatherMapDestination";
	private static final Logger LOGGER = LoggerFactory.getLogger(WeatherServlet.class);
	
	@Resource
    private TenantContext  tenantContext;
	
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
				HttpURLConnection urlConnection = null;
				String cityName = httpRequest.getParameter("cityName");				 
				
				Context ctx = new InitialContext();
	            ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
	            
	            // Get destination configuration for "destinationName"
	            DestinationConfiguration destConfiguration = configuration.getConfiguration(destinationName);
	            if (destConfiguration == null) {
	            	httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	                        String.format("Destination %s is not found.", destinationName));
	                return;
	            }
	            
	            // Get the destination URL
	            String openWeatherURL = String.format("%s?cityName=%s&appid=%s", destConfiguration.getProperty("URL"),cityName,APP_ID);            		
	            URL url = new URL(openWeatherURL);
	            
	            String proxyType = destConfiguration.getProperty("ProxyType");
	            Proxy proxy = getProxy(proxyType);
	            
	            urlConnection = (HttpURLConnection) url.openConnection(proxy);	            
	            
	            // Insert the required header in the request for on-premise destinations
	            injectHeader(urlConnection, proxyType);
	            
	            String weatherResponse = getResponse(urlConnection);
							
				//JSONObject jsonObject = new JSONObject();	
				//jsonObject.put("temperature", "12");		
				//String result = jsonObject.toString();
				httpResponse.setContentType("application/json");							  
				out.print(weatherResponse);				
			}catch(Exception e) {
				LOGGER.error("Connectivity operation failed", e);
				httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}finally {
				out.flush();
			}
			
	out.flush();
		}else {
			httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not allowed!");		
		}
	}

	private String getResponse(HttpURLConnection conn) throws IOException {       
		StringBuilder response = new StringBuilder();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		}finally {
			if(in!=null)
				in.close();
		}
		
        return response.toString();
	}
	
	
	private Proxy getProxy(String proxyType) {
        Proxy proxy = Proxy.NO_PROXY;
        String proxyHost = null;
        String proxyPort = null;

        if (ON_PREMISE_PROXY.equals(proxyType)) {
            // Get proxy for on-premise destinations
            proxyHost = System.getenv("HC_OP_HTTP_PROXY_HOST");
            proxyPort = System.getenv("HC_OP_HTTP_PROXY_PORT");
        } else {
            // Get proxy for internet destinations
            proxyHost = System.getProperty("https.proxyHost");
            proxyPort = System.getProperty("https.proxyPort");
        }

        if (proxyPort != null && proxyHost != null) {
            int proxyPortNumber = Integer.parseInt(proxyPort);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPortNumber));
        }

        return proxy;
    }

    private void injectHeader(HttpURLConnection urlConnection, String proxyType) {
        if (ON_PREMISE_PROXY.equals(proxyType)) {
            // Insert header for on-premise connectivity with the consumer account name
            urlConnection.setRequestProperty("SAP-Connectivity-ConsumerAccount",
                    tenantContext.getTenant().getAccount().getId());
        }
    }

	
}
