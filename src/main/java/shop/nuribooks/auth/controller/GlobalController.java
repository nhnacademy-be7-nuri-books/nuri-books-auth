package shop.nuribooks.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GlobalController {
	private final ApplicationInfoManager applicationInfoManager;

	@PostMapping("/actuator/shutdown")
	@ResponseStatus(value = HttpStatus.OK)
	public void quit() {
		applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
	}
}