<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Login to HangPro!</title>
    <link rel="stylesheet" href="login.css">

</head>
<body>

<div id="login">
    <form action="/user/login" enctype="application/x-www-form-urlencoded" method="post">
        <label for="username">Username:</label>
        <input id="username" minlength="4" type="text" name="username">
        <label for="password">Password:</label>
        <input id="password" type="password" minlength="6" name="password">
        <input type="submit" value="Login" id="connect">
    </form>
    <a href="/user/signup">Signup instead!</a>
</div>


</body>
</html>