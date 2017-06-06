package com.example.synerzip.recircle_android.models.UserMessages;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Prajakta Patil on 2/6/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */

@Getter
@Setter

class UserReqProductDetails {

    private String product_title;

    private String product_manufacturer_id;

    private ProductReqManufacturer product_manufacturer;

}
