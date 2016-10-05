/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.google.android.apps.forscience.whistlepunk.devicemanager;

import android.app.PendingIntent;

class TestDeviceOptionsPresenter implements DeviceOptionsPresenter {
    public String experimentId;
    public String sensorId;

    @Override
    public void refreshScanningUI() {

    }

    @Override
    public void showDeviceOptions(String experimentId, String sensorId,
            PendingIntent externalSettingsIntent) {
        this.experimentId = experimentId;
        this.sensorId = sensorId;
    }
}
