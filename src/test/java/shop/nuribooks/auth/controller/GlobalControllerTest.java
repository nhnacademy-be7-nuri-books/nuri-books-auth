// package shop.nuribooks.auth.controller;
//
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.web.servlet.MockMvc;
//
// import com.netflix.appinfo.ApplicationInfoManager;
//
// @WebMvcTest(GlobalController.class)
// public class GlobalControllerTest {
// 	@Autowired
// 	private MockMvc mockMvc;
//
// 	@MockBean
// 	private ApplicationInfoManager applicationInfoManager;
//
// 	@Test
// 	public void test() throws Exception {
// 		mockMvc.perform(post("/actuator/shutdown"))
// 			.andExpect(status().isOk());
// 	}
// }
