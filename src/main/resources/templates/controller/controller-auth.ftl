<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>ProHang | Controller-Auth</title>
    <style>
        @import url("https://fonts.googleapis.com/css2?family=Inter&display=swap");

        * {
            padding: 0;
            margin: 0;
            box-sizing: border-box;
            font-family: Inter;

        }

        body {
            width: 100%;
            background: #1a2226;
            color: white;
        }

        section {
            width: 100%;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
        }

        .container {
            border-radius: 12px;
            background-color: rgb(19, 25, 28);
            width: 90%;
            max-width: 500px;
            padding: 50px 20px;
            text-align: center;
        }

        .title {
            font-size: 25px;
            margin-bottom: 30px;
        }

        #otp-form {
            width: 100%;
            display: flex;
            gap: 20px;
            align-items: center;
            justify-content: center;
        }

        #otp-form input {
            border: none;
            background-color: #121517;
            color: white;
            font-size: 32px;
            text-align: center;
            padding: 10px;
            width: 100%;
            max-width: 70px;
            height: 70px;
            border-radius: 4px;
            outline: 2px solid rgb(66, 66, 66);
            text-transform: uppercase;
        }

        #otp-form input:focus-visible {
            outline: 2px solid royalblue;
        }

        #otp-form input.filled {
            outline: 2px solid rgb(7, 192, 99);
        }

        #verify-btn {
            cursor: pointer;
            display: inline-block;
            margin-top: 30px;
            background: royalblue;
            color: white;
            padding: 7px 10px;
            border-radius: 4px;
            font-size: 16px;
            border: none;
        }


    </style>


</head>
<body>
<section>
    <div class="container">
        <h1 class="title">Enter board's OTP</h1>
        <form action="/control/auth" id="otp-form" enctype="application/x-www-form-urlencoded" method="post">
            <label for="0"></label><input type="text" name="0" class="otp-input" id="0" maxlength="1" minlength="1">

            <label for="1"></label><input type="text" name="1" class="otp-input" id="1" maxlength="1" minlength="1">

            <label for="2"></label><input type="text" name="2" class="otp-input" id="2" maxlength="1" minlength="1">

            <label for="3"></label><input type="text" name="3" class="otp-input" id="3" maxlength="1" minlength="1">

            <label for="4"></label><input type="text" name="4" class="otp-input" id="4" maxlength="1" minlength="1">

            <label for="5"></label><input type="text" name="5" class="otp-input" id="5" maxlength="1" minlength="1">


        </form>
        <input type="button" id="verify-btn" name="Login" value="Login">
    </div>
    <div><a href="/user">Back to profile.</a></div>
</section>
</body>
<script>

    const urlParams = new URLSearchParams(window.location.search);
    let reason = null
    switch (urlParams.get("reason")) {
        case "length":
            reason = "Please enter six digits into the OTP area!"
            break
        case "notfound":
            reason = "Could not find board with OTP with entered value!"
            break
    }
    if (reason != null) {
        alert(reason)
    }

    const form = document.querySelector("#otp-form");
    const inputs = document.querySelectorAll(".otp-input");
    const verifyBtn = document.querySelector("#verify-btn");

    const isAllInputFilled = () => {
        return Array.from(inputs).every((item) => item.value);
    };

    const getOtpText = () => {
        let text = "";
        inputs.forEach((input) => {
            text = text + input.value;
        });
        return text;
    };

    const verifyOTP = () => {
        if (isAllInputFilled()) {
            document.getElementById("otp-form").submit()
        }
    };

    const toggleFilledClass = (field) => {
        if (field.value) {
            field.classList.add("filled");
        } else {
            field.classList.remove("filled");
        }
    };

    form.addEventListener("input", (e) => {
        const target = e.target;
        const value = target.value;
        console.log({target, value});
        toggleFilledClass(target);
        if (target.nextElementSibling) {
            target.nextElementSibling.focus();
        }
        verifyOTP();
    });

    inputs.forEach((input, currentIndex) => {
        // fill check
        toggleFilledClass(input);

        // paste event
        input.addEventListener("paste", (e) => {
            e.preventDefault();
            const text = e.clipboardData.getData("text");
            console.log(text);
            inputs.forEach((item, index) => {
                if (index >= currentIndex && text[index - currentIndex]) {
                    item.focus();
                    item.value = text[index - currentIndex] || "";
                    toggleFilledClass(item);
                    verifyOTP();
                }
            });
        });

        // backspace event
        input.addEventListener("keydown", (e) => {
            if (e.keyCode === 8) {
                e.preventDefault();
                input.value = "";
                // console.log(input.value);
                toggleFilledClass(input);
                if (input.previousElementSibling) {
                    input.previousElementSibling.focus();
                }
            } else {
                if (input.value && input.nextElementSibling) {
                    input.nextElementSibling.focus();
                }
            }
        });
    });

    verifyBtn.addEventListener("click", () => {
        verifyOTP();
    });

</script>
</html>
