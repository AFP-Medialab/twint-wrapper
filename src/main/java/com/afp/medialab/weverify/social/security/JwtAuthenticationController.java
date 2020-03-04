package com.afp.medialab.weverify.social.security;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.afp.medialab.weverify.social.security.model.FusionAuthDataConverter;
import com.afp.medialab.weverify.social.security.model.JwtCreateAccessCodeRequest;
import com.afp.medialab.weverify.social.security.model.JwtCreateRegistrationRequest;
import com.afp.medialab.weverify.social.security.model.JwtLoginRequest;
import com.afp.medialab.weverify.social.security.model.JwtLoginResponse;
import com.afp.medialab.weverify.social.security.model.JwtRefreshTokenResponse;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;

import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.MemberRequest;
import io.fusionauth.domain.api.MemberResponse;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.jwt.RefreshRequest;
import io.fusionauth.domain.api.jwt.RefreshResponse;
import io.fusionauth.domain.api.passwordless.PasswordlessLoginRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessSendRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartResponse;
import io.fusionauth.domain.jwt.DeviceInfo;
import io.fusionauth.domain.jwt.RefreshToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Authentication controller, based on JWT and providing passwordless
 * authentication.
 * 
 * @author Bertrand GOUPIL
 * @author <a href="mailto:eric@rickspirit.io">Eric SCHAEFFER</a>
 */
@RestController
@CrossOrigin
@RequestMapping(path = "/api/v1/auth")
@Api(description = "User authentication API.")
public class JwtAuthenticationController {

	private static Logger Logger = LoggerFactory.getLogger(JwtAuthenticationController.class);

	@Value("${security.fusionAuth.url}")
	private String fusionAuthUrl;

	@Value("${security.fusionAuth.apiKey}")
	private String fusionAuthApiKey;

	@Value("${security.auth.register-user.groupId}")
	private UUID registerUserGroupId;

	@Value("${security.auth.twint.applicationId}")
	private UUID twintApplicationId;

	private FusionAuthClient fusionAuthClient;

	@Value("${spring.profiles.active:}")
	private String activeProfiles;
	private boolean initialized = false;

	private boolean devProfile = false;

	/**
	 * Constructor.
	 */
	public JwtAuthenticationController() {
		super();
	}

	/**
	 * Register a new user.
	 * 
	 * @param createRegistrationRequest
	 */
	@RequestMapping(path = "/registration", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Create a new user registration request on the system.",
			notes = "Create a user registration request on the system, subject to moderation."
					+ " Once validated, user will be notified by email and will be able to request an authentication code."
					+ "\n" + "The operation will not return any error in case of invalid or duplicated demand.")
	@ApiResponses(
			value = { @ApiResponse(code = 204, message = "User registration request has been received correctly."),
					@ApiResponse(code = 400, message = "Request is incomplete or malformed."),
					@ApiResponse(code = 500, message = "An internal error occured during request processing.") })
	public void createRegistration(@Valid @RequestBody @ApiParam(value = "User registration information.",
			required = true) JwtCreateRegistrationRequest createRegistrationRequest) {
		Logger.debug("Create User with request {}", createRegistrationRequest);

		String userEmail = createRegistrationRequest.email;

		// Check if user already exists
		Logger.debug("Looking for user {}", userEmail);
		ClientResponse<UserResponse, Errors> getUserResponse = getFusionAuthClient().retrieveUserByEmail(userEmail);
		Logger.debug("GetUser response: {}", formatClientResponse(getUserResponse));
		if (getUserResponse.wasSuccessful()) {
			// User already exists, silently returning
			Logger.info("Duplicate user {} request, silently returning", userEmail);
			return;
		}

		// New User to create
		User user = new User();
		user.email = userEmail;
		// TODO: generate random strong password
		user.password = UUID.randomUUID().toString();
		user.firstName = createRegistrationRequest.firstName;
		user.lastName = createRegistrationRequest.lastName;
		Map<String, Object> userData = new HashMap<String, Object>();
		Optional.ofNullable(createRegistrationRequest.organization).ifPresent(s -> userData.put("organization", s));
		Optional.ofNullable(createRegistrationRequest.organizationRole)
				.ifPresent(s -> userData.put("organizationRole", s.name()));
		Optional.ofNullable(createRegistrationRequest.organizationRoleOther)
				.ifPresent(s -> userData.put("organizationRoleOther", s));
		user.data = userData;
		Optional.ofNullable(createRegistrationRequest.preferredLanguages).map(l -> user.preferredLanguages.addAll(l));
		user.timezone = createRegistrationRequest.timezone;

		// Create user
		Logger.debug("Creating user {} with data: {}", userEmail, user);
		ClientResponse<UserResponse, Errors> createUserResponse = getFusionAuthClient().createUser(null,
				new UserRequest(false, false, user));
		Logger.debug("CreateUser response: {}", formatClientResponse(createUserResponse));

		if (!createUserResponse.wasSuccessful()) {
			// Stop and return error message
			Logger.warn("Service error creating new user {}: {}", userEmail, formatClientResponse(createUserResponse));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Service was unable to process your request");
		}

		UserResponse userResponse = createUserResponse.successResponse;
		UUID userId = userResponse.user.id;

		// TODO: Add user registration on application

		// Add user to created users group
		UUID userGroupUUID = this.registerUserGroupId;
		GroupMember groupMember = new GroupMember();
		groupMember.groupId = userGroupUUID;
		groupMember.userId = userId;
		MemberRequest memberRequest = new MemberRequest(userGroupUUID, Collections.singletonList(groupMember));
		Logger.debug("Adding user {} to created users group with data: {}", userEmail, groupMember);
		ClientResponse<MemberResponse, Errors> createGroupMembersResponse = getFusionAuthClient()
				.createGroupMembers(memberRequest);
		Logger.debug("CreateGroupMember response: {}", formatClientResponse(createGroupMembersResponse));

		if (!createGroupMembersResponse.wasSuccessful()) {
			Logger.warn("Service error adding user {} to created users group: {}", userEmail,
					formatClientResponse(createGroupMembersResponse));
		}

		// To enforce security, deactivate user account
		Logger.debug("Deactivating user {}", userEmail);
		ClientResponse<Void, Errors> deactivateUserResponse = getFusionAuthClient().deactivateUser(userId);
		Logger.debug("Deactivating user response: {}", formatClientResponse(deactivateUserResponse));

		if (!deactivateUserResponse.wasSuccessful()) {
			Logger.warn("Service error deactivating user {}: {}", userEmail,
					formatClientResponse(deactivateUserResponse));
		}

		return;
	}

	/**
	 * Request application access code API.
	 * 
	 * @param createAccessCodeRequest
	 */
	@RequestMapping(path = "/accesscode", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	// @RequestMapping(path = "/accesscode", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Request an access code for user authentication, sent by email.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Access code request has been received correctly."),
			@ApiResponse(code = 400, message = "Request is incomplete or malformed."),
			@ApiResponse(code = 500, message = "An internal error occured during request processing.") })
	public void createAccessCode(@Valid @RequestBody JwtCreateAccessCodeRequest createAccessCodeRequest) {
		Logger.debug("Create access code with request {}", createAccessCodeRequest);

		String userEmail = createAccessCodeRequest.email;

		// Check if user exists
		Logger.debug("Looking for user {}", userEmail);
		ClientResponse<UserResponse, Errors> getUserResponse = getFusionAuthClient().retrieveUserByEmail(userEmail);
		Logger.debug("GetUser response: {}", formatClientResponse(getUserResponse));
		if (!getUserResponse.wasSuccessful()) {
			if (getUserResponse.status == 404) {
				// User not found
				Logger.debug("User {} not found", userEmail);
				// throw new ServiceBadRequestException("Unable to process request for provided
				// email");
				return;
			}
			// Error in service call
			Logger.warn("Service error getting user {}: {}", userEmail, formatClientResponse(getUserResponse));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Service was unable to process your request");
		}

		// Check user can log (active, has registration/group for application
		// access)
		User user = getUserResponse.successResponse.user;
		if (!user.active) {
			// User is deactivated
			Logger.debug("User {} is deactivated", userEmail);
			return;
		}
		// Check registration/group for application access
		Optional<UserRegistration> userRegistrationOpt = Optional
				.ofNullable(user.getRegistrationForApplication(this.twintApplicationId));
		if (!userRegistrationOpt.isPresent()) {
			// User doesn't have access rights to application
			Logger.debug("User {} doesn't have a registration for application {}", userEmail, this.twintApplicationId);
			return;
		}

		// Creating authentication access code
		Logger.debug("Creating authentication access code for user {}", userEmail);
		PasswordlessStartRequest pwdStartRequest = new PasswordlessStartRequest();
		pwdStartRequest.applicationId = this.twintApplicationId;
		pwdStartRequest.loginId = userEmail;

		ClientResponse<PasswordlessStartResponse, Errors> pwdStartResponse = getFusionAuthClient()
				.startPasswordlessLogin(pwdStartRequest);
		Logger.debug("PasswordLessLogin response: {}", formatClientResponse(pwdStartResponse));
		if (!pwdStartResponse.wasSuccessful()) {
			Logger.warn("Service error creating authentication access code for user {}: {}", userEmail,
					formatClientResponse(pwdStartResponse));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Service was unable to process your request");
		}

		// Send authentication access code to user (email)
		Logger.debug("Sending authentication access code for user {}", userEmail);
		PasswordlessSendRequest pwdSendRequest = new PasswordlessSendRequest();
		pwdSendRequest.code = pwdStartResponse.successResponse.code;
		ClientResponse<Void, Errors> pwdSendResponse = getFusionAuthClient().sendPasswordlessCode(pwdSendRequest);
		Logger.debug("Send authentication response: {}", formatClientResponse(pwdSendResponse));
		if (!pwdSendResponse.wasSuccessful()) {
			Logger.warn("Service error sending authentication access code for user {}: {}", userEmail,
					formatClientResponse(pwdSendResponse));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Service was unable to process your request");
		}

		return;
	}

	/**
	 * @param loginRequest
	 * @param httpRequest
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(path = "/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Login a user using an access code.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Login has been successful."),
			@ApiResponse(code = 400, message = "Request is incomplete or malformed."),
			@ApiResponse(code = 403, message = "Invalid credentials, login has been refused."),
			@ApiResponse(code = 409, message = "User account state is preventing login."),
			@ApiResponse(code = 410, message = "User account is expired."),
			@ApiResponse(code = 500, message = "An internal error occured during request processing.") })
	public JwtLoginResponse login(@Valid @RequestBody JwtLoginRequest loginRequest, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		Logger.debug("Login with request {}", loginRequest);

		PasswordlessLoginRequest pwdLessLoginRequest = new PasswordlessLoginRequest();
		pwdLessLoginRequest.applicationId = this.twintApplicationId;
		pwdLessLoginRequest.code = loginRequest.code;
		pwdLessLoginRequest.ipAddress = HttpRequestUtils.getClientIpAddress();
		Logger.debug("Found IP adress: {}", pwdLessLoginRequest.ipAddress);
		Map<String, String> userAgentInfo = HttpRequestUtils.getUserAgentInformation();
		Logger.debug("Found User Agent info: {}", userAgentInfo);
		if (!userAgentInfo.isEmpty()) {
			pwdLessLoginRequest.metaData = new RefreshToken.MetaData();
			pwdLessLoginRequest.metaData.device = new DeviceInfo();
			pwdLessLoginRequest.metaData.device.name = userAgentInfo.get("DeviceName");
			String deviceTypeStr = userAgentInfo.get("DeviceClass");
			DeviceInfo.DeviceType deviceType = null;
			if (deviceTypeStr != null) {
				if ("Desktop".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.DESKTOP;
				} else if ("Anonymized".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.UNKNOWN;
				} else if ("Unknown".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.UNKNOWN;
				} else if ("Mobile".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.MOBILE;
				} else if ("Tablet".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.TABLET;
				} else if ("Phone".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.MOBILE;
				} else if ("Watch".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.MOBILE;
				} else if ("Virtual Reality".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.OTHER;
				} else if ("eReader".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.MOBILE;
				} else if ("Set-top box".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.TV;
				} else if ("TV".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.TV;
				} else if ("Game Console".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.TV;
				} else if ("Handheld Game Console".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.TV;
				} else if ("Voice".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.OTHER;
				} else if ("Robot".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.SERVER;
				} else if ("Robot Mobile".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.SERVER;
				} else if ("Robot Imitator".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.SERVER;
				} else if ("Hacker".equalsIgnoreCase(deviceTypeStr)) {
					deviceType = DeviceInfo.DeviceType.OTHER;
				} else {
					deviceType = DeviceInfo.DeviceType.UNKNOWN;
				}
			}
			pwdLessLoginRequest.metaData.device.type = deviceType;
			// TODO: better description...
			pwdLessLoginRequest.metaData.device.description = userAgentInfo.get("Useragent");
		}
		Logger.debug("Login user with information: {}", pwdLessLoginRequest);
		ClientResponse<LoginResponse, Errors> pwdLessLoginResponse = getFusionAuthClient()
				.passwordlessLogin(pwdLessLoginRequest);
		Logger.debug("Login response: {}", formatClientResponse(pwdLessLoginResponse));

		if (!pwdLessLoginResponse.wasSuccessful()) {
			if (pwdLessLoginResponse.status >= 500) {
				Logger.warn("FusionAuth error: {}", formatClientResponse(pwdLessLoginResponse));
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Service was unable to process your request");
			}
			if (pwdLessLoginResponse.status == 400) {
				Logger.warn("Error while calling FusionAuth service: {}", formatClientResponse(pwdLessLoginResponse));
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Service was unable to process your request");
			}
			if (pwdLessLoginResponse.status == 409) {
				// User blocked
				Logger.debug("Login user is blocked: {}", formatClientResponse(pwdLessLoginResponse));
				throw new ResponseStatusException(HttpStatus.CONFLICT, "User account is blocked");
			}
			if (pwdLessLoginResponse.status == 410) {
				// User expired
				Logger.debug("Login user is expired: {}", formatClientResponse(pwdLessLoginResponse));
				throw new ResponseStatusException(HttpStatus.GONE, "User account is expired");
			}
			// Return 403
			Logger.debug("Login user is refused: {}", formatClientResponse(pwdLessLoginResponse));
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		}

		// TODO: check user email address??

		// TODO: logout user before returning?
		LoginResponse respContent = pwdLessLoginResponse.successResponse;
		if ((respContent.token == null) || (respContent.user == null)) {
			// User state prevent login
			Logger.debug("Login response without token or user: {}", formatClientResponse(pwdLessLoginResponse));
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User account is blocked");
		}
		if (!respContent.user.active) {
			// User inactive
			Logger.debug("Login user is inactive: {}", formatClientResponse(pwdLessLoginResponse));
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User account is blocked");
		}
		if (respContent.user.getRegistrationForApplication(this.twintApplicationId) == null) {
			// User not registered for application
			Logger.debug("Login user has no registration for application {}: {}", this.twintApplicationId,
					formatClientResponse(pwdLessLoginResponse));
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		}

		// Convert response data
		JwtLoginResponse loginResponse = new JwtLoginResponse();
		loginResponse.token = respContent.token;
		loginResponse.user = FusionAuthDataConverter.toJwtUser(respContent.user, this.twintApplicationId);

		// Add refresh token as a secure http only cookie
		httpResponse.addCookie(createRefreshTokenCookie(respContent.refreshToken));

		Logger.debug("Returning response: {}", loginResponse);
		return loginResponse;
	}

	/**
	 * Refresh a JWT access token.
	 * 
	 * @param refreshToken
	 * @return
	 */
	@RequestMapping(path = "/refreshtoken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Refresh a user access token.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Token has been successfuly refreshed."),
			@ApiResponse(code = 400, message = "Request is incomplete or malformed."),
			@ApiResponse(code = 401,
					message = "Invalid or expired refresh token, refresh has been refused, user is logged out."),
			@ApiResponse(code = 500, message = "An internal error occured during request processing.") })
	public JwtRefreshTokenResponse refreshToken(@CookieValue(name = "refresh_token") String refreshToken) {
		Logger.debug("Refresh token for {}", refreshToken);

		RefreshRequest refreshRequest = new RefreshRequest();
		refreshRequest.refreshToken = refreshToken;

		Logger.debug("Refreshing token with request: {}", refreshRequest);
		ClientResponse<RefreshResponse, Errors> refreshClientResponse = getFusionAuthClient()
				.exchangeRefreshTokenForJWT(refreshRequest);
		Logger.debug("Refresh response: {}", formatClientResponse(refreshClientResponse));

		if (!refreshClientResponse.wasSuccessful()) {
			if (refreshClientResponse.status >= 500) {
				Logger.warn("FusionAuth call error: {}", formatClientResponse(refreshClientResponse));
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Service was unable to process your request");
			}
			Logger.debug("Refresh refused: {}", formatClientResponse(refreshClientResponse));
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
		}

		JwtRefreshTokenResponse refreshTokenResponse = new JwtRefreshTokenResponse();
		refreshTokenResponse.token = refreshClientResponse.successResponse.token;

		Logger.debug("Returning response: {}", refreshTokenResponse);
		return refreshTokenResponse;
	}

	@RequestMapping(path = "/logout", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Object logout(Object logoutRequest) throws Exception {
		// TODO
		return null;
	}

	/**
	 * Validation exception handler method.
	 * 
	 * @param request
	 * @param exception
	 * @return
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, Object> handleValidationExceptions(HttpServletRequest request,
			MethodArgumentNotValidException exception) {
		Map<String, Object> respBody = new HashMap<>();

		respBody.put("timestamp", Instant.now().toString());
		respBody.put("status", HttpStatus.BAD_REQUEST.value());
		respBody.put("path", request.getRequestURI());

		BindingResult bindingResult = exception.getBindingResult();
		List<Map<String, Object>> errors = new ArrayList<Map<String, Object>>();
		bindingResult.getAllErrors().forEach((err) -> {
			Map<String, Object> error = new HashMap<String, Object>();
			error.put("error", (err instanceof FieldError) ? ((FieldError) err).getField() : err.getObjectName());
			error.put("message", err.getDefaultMessage());
			errors.add(error);
		});
		if (errors.size() > 1) {
			respBody.put("errors", errors.toArray());
		} else {
			respBody.putAll(errors.get(0));
		}

		return respBody;
	}

	// @RequestMapping(path = "/authenticate", method = RequestMethod.POST)
	// public FusionLoginResponse createAuthenticationToken(@RequestBody JwtRequest
	// authenticationRequest)
	// throws Exception {
	//
	// FusionLoginRequest fusionLoginRequest = new FusionLoginRequest();
	// fusionLoginRequest.setLoginId(authenticationRequest.getUsername());
	// fusionLoginRequest.setPassword(authenticationRequest.getPassword());
	// fusionLoginRequest.setApplicationId(fusionClientId);
	//
	// FusionLoginResponse response = new RestTemplate().postForObject(fusionUri,
	// fusionLoginRequest,
	// FusionLoginResponse.class);
	//
	// return response;
	// }

	/**
	 * @return the fusionAuthClient
	 */
	private FusionAuthClient getFusionAuthClient() {
		initFusionAuthClient();
		return this.fusionAuthClient;
	}

	/**
	 * Check and initialize if required the FusionAuth java client.
	 */
	private void initFusionAuthClient() {
		// Init FusionAuth client
		if (this.fusionAuthClient == null) {
			this.fusionAuthClient = new FusionAuthClient(this.fusionAuthApiKey, this.fusionAuthUrl);
		}
	}

	/**
	 * Create a secure http only cookie for the JWT refresh token.
	 * 
	 * @param refreshToken
	 * @return
	 */
	private Cookie createRefreshTokenCookie(String refreshToken) {
		Cookie cookie = new Cookie("refresh_token", refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		if (this.isDevProfile()) {
			cookie.setSecure(false);
		}
		// Only to refresh token endpoint
		String baseAppPath = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getServletContext().getContextPath();
		cookie.setPath(baseAppPath + "/api/v1/auth/refreshtoken");
		// Max age of 2 months
		cookie.setMaxAge(60 * 24 * 60 * 60);
		return cookie;
	}

	/**
	 * Format a ClientResponse object for logging.
	 * 
	 * @param response
	 * @return
	 */
	private String formatClientResponse(ClientResponse<?, Errors> response) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("url ").append(String.valueOf(response.url));
		strBuffer.append(", ").append("status ").append(response.status);
		if (response.successResponse != null) {
			strBuffer.append(", ").append("success-response ").append(String.valueOf(response.successResponse));
		}
		if (response.errorResponse != null) {
			strBuffer.append(", ").append("error-response ").append(String.valueOf(response.errorResponse));
		}
		if (response.exception != null) {
			strBuffer.append(", ").append(String.valueOf(response.exception));
		}
		return strBuffer.toString();
	}

	/**
	 * @return the devProfile
	 */
	private boolean isDevProfile() {
		init();
		return devProfile;
	}

	// @PostConstruct
	public void init() {
		if (this.initialized) {
			return;
		}
		Logger.debug("Active Spring Profiles: {}", this.activeProfiles);
		this.devProfile = checkDevProfile(this.activeProfiles);
		Logger.debug("Dev mode: {}", this.devProfile);
		this.initialized = true;
	}

	private static boolean checkDevProfile(String profiles) {
		if (profiles == null || "".equals(profiles.trim())) {
			return true;
		}
		for (String p : profiles.split(",")) {
			if ("dev".equalsIgnoreCase(p.trim())) {
				return true;
			}
		}
		return false;
	}
}
