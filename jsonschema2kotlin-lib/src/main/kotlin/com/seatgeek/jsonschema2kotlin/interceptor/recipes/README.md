# Recipes

The following pre-existing implementations provide added functionality that can easily be added in to code generation

## ParcelizeDataClassInterceptor

Intercepts data class definition and adds the `Parcelable` superclass and the `@Parcelize` annotation to
enable [Android Parcelable](https://developer.android.com/reference/android/os/Parcelable) support 
