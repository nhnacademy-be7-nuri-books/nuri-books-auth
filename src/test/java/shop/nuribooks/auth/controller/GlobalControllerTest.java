package shop.nuribooks.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GlobalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApplicationInfoManager applicationInfoManager;

    @InjectMocks
    private GlobalController globalController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
        mockMvc = MockMvcBuilders.standaloneSetup(globalController).build();
    }

    @Test
    void testQuit() throws Exception {
        // Mock 설정
        doNothing().when(applicationInfoManager).setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);

        // 요청 실행 및 검증
        mockMvc.perform(post("/actuator/shutdown"))
                .andExpect(status().isOk());

        // verify를 통해 호출 여부 확인
        verify(applicationInfoManager, times(1)).setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
    }
}
