package security.backend.jwtsecurityserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource controller
 */
@RestController
public class ResourceController {

	@RequestMapping("/hellouser")
	public String getUser()
	{
		return "Hello User";
	}

	@RequestMapping("/helloadmin")
	public String getAdmin()
	{
		return "Hello Admin";
	}

}
