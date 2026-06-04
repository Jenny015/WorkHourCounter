package com.example.workhourcounter

class Config(){
    companion object {
        fun validateHourlyInput(input: String, input2: String = "0"): Boolean {
            // Return true temporarily if the user clears the text field or types just a decimal point
            if ((input.isEmpty() || input == ".") && (input2.isEmpty() || input2 == ".")) return true

            val floatValue = input.toFloatOrNull()
            val floatValue2 = input2.toFloatOrNull()
            return floatValue != null && floatValue2 != null && floatValue+floatValue2 in 0.1f..24.0f
        }
        var fontSize: Int = 1
        var baseWorkHour: Float = 8.0f
        var MAX_TEXT_INPUT: Int = 20
    }

}
