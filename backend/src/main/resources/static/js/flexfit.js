document.addEventListener("DOMContentLoaded", function () {
    initMobileSidebar();
    initPasswordToggles();
    initRegistrationWizard();
});

function initMobileSidebar() {
    var sidebar = document.getElementById("appSidebar");
    var backdrop = document.getElementById("sidebarBackdrop");
    var openBtn = document.getElementById("sidebarOpenBtn");
    var closeBtn = document.getElementById("sidebarCloseBtn");

    if (!sidebar) {
        return;
    }

    function setOpen(isOpen) {
        sidebar.classList.toggle("is-open", isOpen);
        if (backdrop) {
            backdrop.classList.toggle("is-open", isOpen);
        }
    }

    if (openBtn) {
        openBtn.addEventListener("click", function () {
            setOpen(true);
        });
    }
    if (closeBtn) {
        closeBtn.addEventListener("click", function () {
            setOpen(false);
        });
    }
    if (backdrop) {
        backdrop.addEventListener("click", function () {
            setOpen(false);
        });
    }
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            setOpen(false);
        }
    });
}

function initPasswordToggles() {
    var buttons = document.querySelectorAll(".toggle-password-btn");
    buttons.forEach(function (btn) {
        btn.addEventListener("click", function () {
            var targetId = btn.getAttribute("data-toggle-target");
            var input = document.getElementById(targetId);
            if (!input) {
                return;
            }
            var showing = input.type === "text";
            input.type = showing ? "password" : "text";
            btn.textContent = showing ? "Show" : "Hide";
        });
    });
}

function initRegistrationWizard() {
    var wizard = document.getElementById("registrationWizard");
    if (!wizard) {
        return;
    }

    var steps = Array.from(wizard.querySelectorAll(".wizard-step"));
    var totalSteps = steps.length;
    var currentStep = parseInt(wizard.getAttribute("data-initial-step") || "1", 10);
    if (isNaN(currentStep) || currentStep < 1 || currentStep > totalSteps) {
        currentStep = 1;
    }

    var stepIndicator = document.getElementById("stepIndicator");
    var stepTitle = document.getElementById("stepTitle");
    var progressFill = document.getElementById("wizardProgressFill");
    var prevBtn = document.getElementById("prevStepBtn");
    var nextBtn = document.getElementById("nextStepBtn");
    var submitBtn = document.getElementById("submitRegisterBtn");
    var errorBox = document.getElementById("wizardClientError");

    function showError(msg) {
        if (!errorBox) {
            return;
        }
        if (msg) {
            errorBox.textContent = msg;
            errorBox.classList.remove("hidden");
        } else {
            errorBox.textContent = "";
            errorBox.classList.add("hidden");
        }
    }

    function renderStep() {
        steps.forEach(function (el) {
            var s = parseInt(el.getAttribute("data-step"), 10);
            el.classList.toggle("hidden", s !== currentStep);
            if (s === currentStep && stepTitle) {
                stepTitle.textContent = el.getAttribute("data-title") || "";
            }
        });

        if (stepIndicator) {
            stepIndicator.textContent = "Step " + currentStep + " of " + totalSteps;
        }
        if (progressFill) {
            progressFill.style.width = ((currentStep / totalSteps) * 100).toFixed(2) + "%";
        }
        if (prevBtn) {
            prevBtn.style.visibility = currentStep === 1 ? "hidden" : "visible";
        }
        if (nextBtn && submitBtn) {
            var isLast = currentStep === totalSteps;
            nextBtn.classList.toggle("hidden", isLast);
            submitBtn.classList.toggle("hidden", !isLast);
        }
        showError("");
    }

    function val(id) {
        var el = document.getElementById(id);
        return el ? el.value.trim() : "";
    }

    function validateStep(step) {
        if (step === 1) {
            var fullName = val("fullName");
            var username = val("username");
            var email = val("email");
            var password = val("regPassword");
            var confirm = val("confirmPassword");
            if (fullName.length < 2) return "Full name must be at least 2 characters.";
            if (!/^[a-zA-Z0-9._-]{3,50}$/.test(username)) {
                return "Username must be 3–50 characters (letters, numbers, ., _, -).";
            }
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return "Please enter a valid email address.";
            if (password.length < 6) return "Password must be at least 6 characters.";
            if (password !== confirm) return "Password and confirmation password do not match.";
        } else if (step === 2) {
            var age = parseInt(val("age"), 10);
            if (isNaN(age) || age < 12 || age > 100) return "Age must be between 12 and 100.";
            if (!val("district")) return "District is required.";
            if (!val("state")) return "State is required.";
        } else if (step === 3) {
            var h = parseFloat(val("heightCm"));
            var cw = parseFloat(val("currentWeightKg"));
            var tw = parseFloat(val("targetWeightKg"));
            if (isNaN(h) || h < 50 || h > 260) return "Height must be between 50 and 260 cm.";
            if (isNaN(cw) || cw < 25 || cw > 300) return "Current weight must be between 25 and 300 kg.";
            if (isNaN(tw) || tw < 25 || tw > 300) return "Target weight must be between 25 and 300 kg.";
        }
        return null;
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", function () {
            if (currentStep > 1) {
                currentStep -= 1;
                renderStep();
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", function () {
            var err = validateStep(currentStep);
            if (err) {
                showError(err);
                return;
            }
            if (currentStep < totalSteps) {
                currentStep += 1;
                renderStep();
            }
        });
    }

    var heightInput = document.getElementById("heightCm");
    var weightInput = document.getElementById("currentWeightKg");
    var bmiValue = document.getElementById("bmiPreviewValue");
    var bmiHint = document.getElementById("bmiPreviewHint");

    function updateBmiPreview() {
        if (!heightInput || !weightInput || !bmiValue || !bmiHint) return;
        var h = parseFloat(heightInput.value);
        var w = parseFloat(weightInput.value);
        if (!isNaN(h) && h > 0 && !isNaN(w) && w > 0) {
            var m = h / 100;
            var bmi = (w / (m * m)).toFixed(1);
            bmiValue.textContent = bmi;
            if (bmi < 18.5) bmiHint.textContent = "Underweight";
            else if (bmi < 25) bmiHint.textContent = "Normal";
            else if (bmi < 30) bmiHint.textContent = "Overweight";
            else bmiHint.textContent = "Obese";
        } else {
            bmiValue.textContent = "--";
            bmiHint.textContent = "Enter height & weight";
        }
    }

    if (heightInput && weightInput) {
        heightInput.addEventListener("input", updateBmiPreview);
        weightInput.addEventListener("input", updateBmiPreview);
        updateBmiPreview();
    }

    var eqBoxes = Array.from(wizard.querySelectorAll('input[name="availableEquipment"]'));
    eqBoxes.forEach(function (box) {
        box.addEventListener("change", function () {
            if (box.value === "None" && box.checked) {
                eqBoxes.forEach(function (other) {
                    if (other !== box) other.checked = false;
                });
            } else if (box.value !== "None" && box.checked) {
                eqBoxes.forEach(function (other) {
                    if (other.value === "None") other.checked = false;
                });
            }
        });
    });

    renderStep();
}
