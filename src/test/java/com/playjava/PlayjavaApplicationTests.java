package com.playjava;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.playjava.entity.MUser;
import com.playjava.service.impl.MUserServiceImpl;

@SpringBootTest
class PlayjavaApplicationTests {

	@Autowired
	private MUserServiceImpl mUserService;

	@Test
	public void testCreateUser() {
		MUser user = new MUser();
		user.setLoginId("test");
		user.setUserName("test");
		user.setEmail("test@example.com");
		user.setPasswordHash("password");
		user.setRole("user");
		user.setStatus("active");
		mUserService.save(user);
	}

	@Test
	public void testFindUser() {
		MUser user = new MUser();
		user = mUserService.getById(user.getUserId());
		assert user != null;
		assert user.getUserName().equals("test");
	}
}
