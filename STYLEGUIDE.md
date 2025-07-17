# Style Guide

**This document provides information on how to customize the style of *Komunumo*.**

## Configuring Custom Styles

To configure custom styles, set the environment variable `KOMUNUMO_CUSTOM_STYLES` to the URL of your custom CSS file. This file must be accessible via HTTPS and contain valid CSS.

```
KOMUNUMO_CUSTOM_STYLES=https://static.example.com/custom.css
```

The maintainer of the *Komunumo* instance is responsible for hosting the custom CSS file as well as any additional resources referenced within it, such as background images, fonts, or logos. All resources must be accessible via HTTPS.

## Styling the Application

You can customize the appearance of the user interface by overriding CSS variables. The following table lists all CSS variables that can be modified to change the UI. You can include these variables in your custom CSS file.

| CSS Variable                | Description                                                                              |
|-----------------------------|------------------------------------------------------------------------------------------|
| --komunumo-background-color | The background color of the main content area.<br/>*Used only for testing this feature.* |

> [!NOTE]  
> When upgrading to a new release of *Komunumo*, new CSS variables may be added or existing ones removed. Please read the release notes to check for any changes and compare your styles before and after the update.
