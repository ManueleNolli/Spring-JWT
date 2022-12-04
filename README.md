# Spring Security and JWT Token

:frowning_person:	Manuele Nolli 

:frowning_person: [Matthias Berchtold](https://github.com/iammatthi)

:frowning_person: [Davis Fusco](https://github.com/davisf20)  

:school:	SUPSI

:calendar:	2022/2023

## Description 

A secure basic webpage with spring security functionalities and JWT tokens for authentication and authorization.

### Structural information

There are two different server:
 - a JWT service server connected with a MySQL DB (backend)
 - a Client Server (frontend)
 
 Both server are based on Spring Boot framework (v. 2.7.5) and use the addable plugin 'Spring security'.

The purpose of the JWT service server (Backend) is to provide a facilities of creation and management of the users with their associate JWT token.

On the other hand, the purpose of the client server is to provide web pages to users. It provides a registration and login page. In addition, the correctness of the token can be verified through two different restricted access pages: adminPage and userPage.

The database server is a MySQL 8.0 with a simple user table:

![User DB table](https://github.com/ManueleNolli/spring-security-jwt-token/blob/main/docs/images/dbTable.jpg)


### Implementations details
The basic idea of the project is to have a server where you can authenticate, after authentication receive a JWT token and send it in the next requests, so that the server checks if you have authorization.

It should be noted how passwords between client browser and client server are sent in clear text, this should be easily resolved via HTTPS protocol. In addition, passwords within the database are encrypted and the token is signed with HS512 alghoritm.
The registration procedure is as follows:

![register](https://github.com/ManueleNolli/spring-security-jwt-token/blob/main/docs/images/registration.png)

The login procedure is as follows:

![login](https://github.com/ManueleNolli/spring-security-jwt-token/blob/main/docs/images/login.png)

The refresh token procedure is as follows:

![login](https://github.com/ManueleNolli/spring-security-jwt-token/blob/main/docs/images/refreshToken.png)

**Note**: The access token is validated with each request and if it is expired but the refresh token is not, the client server will send a request to the JWT server to update the token. The validation task also includes an authorization check based on the user’s role

## REST API

### Backend

 URI | Description 
--- | ---
*POST backend:8080/register* | Used for register a new user. The necessary parameter are: ’username’, ’password’, ’role’ (ROLE_USER or ROLE_ADMIN). The answer will be a 200 Ok HTTP status with the id, username and role of the created user. 
*POST backend:8080/authenticate* | Used for user authentication. The necessary parameter are: ’username’, ’password’. The answer will be a 200 Ok Http status with two JWT token:  (1) **accessToken**: needed to access the pages.  (2) **refreshToken** needed to ask a new accessToken.
*POST backend:8080/refreshtoken* | Used to request a new token. The necessary parameter are: ’refreshToken’. The answer will be a new accessToken.
 
 ### Frontend

 URI | Description 
--- | ---
*GET frontend:8080/* | Login page.
*POST frontend:8080/login* | Send a request to backend:8080/authenticate.
*GET frontend:8080/registration* |  Registration page.
*POST frontend:8080/registration* |  Send a request to backend:8080/register.
*GET frontend:8080/userPage* |  User page.
*GET frontend:8080/adminPage* | User page.
