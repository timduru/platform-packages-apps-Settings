/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.users;

import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.os.UserManager;

public class UserUtils {

    public static Drawable getUserIcon(UserManager um, UserInfo user) {
        if (user.iconPath == null) return null;
        ParcelFileDescriptor fd = um.getUserIcon(user.id);
        if (fd == null) return null;
        Drawable d = Drawable.createFromStream(new ParcelFileDescriptor.AutoCloseInputStream(fd),
                user.iconPath);
        return d;
    }
}