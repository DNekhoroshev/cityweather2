package ru.sberbank.example.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import ru.sberbank.example.services.exception.DestinationNotFoundException;

public class OpenWeatherMapClient {
	private String appId;	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenWeatherMapClient.class);
	private static final String destinationName = "OpenWeatherMapDestination";	
	private static final String ON_PREMISE_PROXY = "OnPremise";	
	
	private final DestinationConfiguration destConfiguration;		
	
	public OpenWeatherMapClient(String appId) throws NamingException, DestinationNotFoundException {	
		this.appId = appId;
		
		Context ctx = new InitialContext();
        ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
		
		this.destConfiguration = configuration.getConfiguration(destinationName);
        if (this.destConfiguration == null) {        	
        	throw new DestinationNotFoundException(String.format("Destination %s is not found.", destinationName));            
        }		
	}

	public String getTempByCity(String city) throws IOException {	       
        
		HttpURLConnection urlConnection = null;
		
		String openWeatherURL = String.format("%s?q=%s&appid=%s", destConfiguration.getProperty("URL"),city,appId);            		
        
        LOGGER.info("URL: "+openWeatherURL);
        
        URL url = new URL(openWeatherURL);
        
        String proxyType = destConfiguration.getProperty("ProxyType");
        Proxy proxy = getProxy(proxyType);
        
        LOGGER.info("PROXY: "+proxyType);
        
        urlConnection = (HttpURLConnection) url.openConnection(proxy);	            
        
        LOGGER.info("Connection opened!");
        
        String weatherResponse = getResponse(urlConnection);

        LOGGER.info("Response: "+weatherResponse);
        
        return extractTemperature(weatherResponse);
	}	
	
	private String extractTemperature(String weatherResponse){
		JSONObject weatherResponseJson = new JSONObject(weatherResponse);	
		JSONObject main = null;
		String result = "<undefined>";
		
		if(weatherResponseJson.has("main")) {
			main = weatherResponseJson.getJSONObject("main");
			if(main.has("temp")) {
				result = String.valueOf(kelvinToCelsium(main.getDouble("temp")));
			}
		}
		
		return result; 
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
	
	private double kelvinToCelsium(double kelvin) {
		double result = kelvin - 273.15d;
		result = result * 100;
		return Math.round(result)/100.0;
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
	
}
