<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Controller-Auth</title>
    <link rel="stylesheet" href="controller-auth.css">
</head>
<body>
<section>
    <div class="container">
        <h1 class="title">Enter OTP</h1>
        <form action="/control/auth" id="otp-form" enctype="application/x-www-form-urlencoded" method="post">
            <label for="0"></label><input type="text" name="0" class="otp-input" id="0" maxlength="1" minlength="1">

            <label for="1"></label><input type="text" name="1" class="otp-input" id="1" maxlength="1" minlength="1">

            <label for="2"></label><input type="text" name="2" class="otp-input" id="2" maxlength="1" minlength="1">

            <label for="3"></label><input type="text" name="3" class="otp-input" id="3" maxlength="1" minlength="1">

            <label for="4"></label><input type="text" name="4" class="otp-input" id="4" maxlength="1" minlength="1">

            <label for="5"></label><input type="text" name="5" class="otp-input" id="5" maxlength="1" minlength="1">

            <input type="submit" id="verify-btn" name="Login">
        </form>
        <p>Developed by Ezra Golombek</p>
    </div>


</section>
</body>
<script>
    const elts = document.getElementsByClassName('otp-input')
    Array.from(elts).forEach(function (elt) {

        elt.addEventListener("keyup", () => {
            // Number 13 is the "Enter" key on the keyboard
            if (elt.value.length === 1) {
                // Focus on the next sibling
                elt.nextElementSibling.focus()
            }
        });
    })
</script>
</html>