package cityweather2;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ru.sberbank.example.services.OpenWeatherMapClient;
import ru.sberbank.example.services.WeatherServlet;

public class WeatherServletTest {
	
	HttpServletRequest reqMock;
	HttpServletResponse respMock;
	PrintWriter outMock;
	
	@Before
	public void init() throws IOException{
		reqMock = mock(HttpServletRequest.class);
		respMock = mock(HttpServletResponse.class);
		outMock = mock(PrintWriter.class);
		
		when(respMock.getWriter()).thenReturn(outMock);
	}
	
	@Test
	public void testGetTempByCityName() throws Exception{		
				
		String expectedResult = "{\"temperature\":\"22.75\"}";
		
		when(reqMock.getMethod()).thenReturn("GET");
		when(reqMock.getParameter("cityName")).thenReturn("Moscow");
		
		OpenWeatherMapClient owmcMock = mock(OpenWeatherMapClient.class);
		
		doAnswer(new Answer<String>() {
			@Override   
			public String answer(InvocationOnMock invocation) throws Exception{			       		       
			       return "22.75";
			   }})
		 .when(owmcMock).getTempByCity(anyString());
		
		WeatherServlet service = new WeatherServlet();
		injectField(service, "client", owmcMock);
				
		service.service(reqMock, respMock);		
		
		verify(outMock,times(1)).print(expectedResult);
		verify(outMock,times(1)).flush();
	}
	
	@Test
	public void testEmptyCityName() throws Exception{		
				
		String expectedResult = "Empty city name is not allowed";
		
		when(reqMock.getMethod()).thenReturn("GET");
		when(reqMock.getParameter("cityName")).thenReturn(null);
		
		OpenWeatherMapClient owmcMock = mock(OpenWeatherMapClient.class);
		
		doAnswer(new Answer<String>() {
			@Override   
			public String answer(InvocationOnMock invocation) throws Exception{			       		       
			       return "22.75";
			   }})
		 .when(owmcMock).getTempByCity(anyString());
		
		WeatherServlet service = new WeatherServlet();
		injectField(service, "client", owmcMock);
				
		service.service(reqMock, respMock);			
		
		verify(outMock,times(0)).print(anyString());
		verify(respMock,times(1)).sendError(400,expectedResult);	
	}
	
	@Test
	public void testDoPost() throws Exception{		
		String expectedResult = "Not allowed";
		
		when(reqMock.getMethod()).thenReturn("POST");
		WeatherServlet service = new WeatherServlet();
		service.service(reqMock, respMock);
				
		verify(respMock,times(1)).sendError(405,expectedResult);
	}
	
	@Test
	public void testDoPut() throws Exception{		
		String expectedResult = "Not allowed";
		
		when(reqMock.getMethod()).thenReturn("POST");
		WeatherServlet service = new WeatherServlet();
		service.service(reqMock, respMock);
		
		verify(respMock,times(1)).sendError(405,expectedResult);
	}
	
	@Test
	public void testDoOptions() throws Exception{		
		String expectedResult = "Not allowed";
		
		when(reqMock.getMethod()).thenReturn("POST");
		WeatherServlet service = new WeatherServlet();
		service.service(reqMock, respMock);
		
		verify(respMock,times(1)).sendError(405,expectedResult);
	}
	
	@Test
	public void testDoDelete() throws Exception{		
		String expectedResult = "Not allowed";
		
		when(reqMock.getMethod()).thenReturn("POST");
		WeatherServlet service = new WeatherServlet();
		service.service(reqMock, respMock);
		
		verify(respMock,times(1)).sendError(405,expectedResult);
	}
	
	@Test
	public void testDoHead() throws Exception{		
		String expectedResult = "Not allowed";
		
		when(reqMock.getMethod()).thenReturn("POST");
		WeatherServlet service = new WeatherServlet();
		service.service(reqMock, respMock);
		
		verify(respMock,times(1)).sendError(405,expectedResult);
	}
	
	private void injectField(Object target,String fieldName,Object value) throws Exception{
		Field injectedField = target.getClass().getDeclaredField(fieldName);
		injectedField.setAccessible(true);
		injectedField.set(target,value);
	}
}
