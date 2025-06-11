plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
   // id("com.google.dagger.hilt.android") version "2.56.2" apply false
    alias(libs.plugins.kotlin.kapt) apply false // Ensure this is consistently aliased and apply false
    alias(libs.plugins.hilt) apply false

}