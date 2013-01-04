/*
 * Copyright (C) 2012 OUYA, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.ouya.sample.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import tv.ouya.console.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tv.ouya.sample.game.Options.Level.*;
import static tv.ouya.sample.game.Options.getInstance;

public class OptionsActivity extends Activity {

    static private Map<Options.Level, RadioButton> levelToRadioButton;
    static private Map<RadioButton, Options.Level> radioButtonToLevel;

    private static final String DEVELOPER_ID = "310a8f51-4d6e-4ae5-bda0-b93878e5f5d0";
    private static final List<Purchasable> PRODUCT_IDENTIFIER_LIST = Arrays.asList(new Purchasable("level_alleyway"), new Purchasable("level_boxy"));

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        levelToRadioButton = new HashMap<Options.Level, RadioButton>();
        levelToRadioButton.put(FREEDOM, ((RadioButton) findViewById(R.id.radio_freedom)));
        levelToRadioButton.put(ALLEYWAY, ((RadioButton) findViewById(R.id.radio_alleyway)));
        levelToRadioButton.put(BOXY, ((RadioButton) findViewById(R.id.radio_boxy)));

        // Create a reverse map
        radioButtonToLevel = new HashMap<RadioButton, Options.Level>();
        for(Options.Level level : levelToRadioButton.keySet()) {
            radioButtonToLevel.put(levelToRadioButton.get(level), level);
        }

        // Initialize the UI
      //  processReceipts(null);
     //   toggleProgressIndicator(true);

     //   OuyaFacade.getInstance().init(this, DEVELOPER_ID);
      //  requestReceipts();

        levelToRadioButton.get(Options.getInstance().getLevel()).setChecked(true);

        Button quit = (Button) findViewById(R.id.back_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        OuyaFacade.getInstance().shutdown();
        super.onDestroy();
    }

    private void requestReceipts() {
        OuyaFacade.getInstance().requestReceipts(new OuyaResponseListener<String>() {
            @Override
            public void onSuccess(String receiptResponse) {
                OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
                List<Receipt> receipts = null;
                try {
                    receipts = helper.decryptReceiptResponse(receiptResponse);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                processReceipts(receipts);
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                processReceipts(null);
                Toast.makeText(OptionsActivity.this, "Error checking purchases!\nAdditional levels not available...\n\nError " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                processReceipts(null);
                Toast.makeText(OptionsActivity.this, "You cancelled checking purchases!\nAdditional levels not available...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private final String purchaseText = " [NEEDS PURCHASING]";
    private void setNeedsPurchaseText(RadioButton rb, boolean needsToBePurchased) {
        String text = rb.getText().toString();

        if (needsToBePurchased) {
            if (!text.endsWith(purchaseText)) {
                text += purchaseText;
            }
        } else {
            if (text.endsWith(purchaseText)) {
                text = text.replace(purchaseText, "");
            }
        }

        rb.setText(text);
    }

    private boolean needsPurchasing(RadioButton rb) {
    		return false;/*
        String text = rb.getText().toString();
        return text.endsWith(purchaseText);*/
    }

    private String getProductIdForLevel(Options.Level level) {
        switch(level) {
            case ALLEYWAY:
                return "level_alleyway";
            case BOXY:
                return "level_boxy";
        }
        return null;
    }

    private void toggleProgressIndicator(boolean progressVisible) {
        findViewById(R.id.progress_indicator).setVisibility(progressVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.levels).setVisibility(progressVisible ? View.GONE : View.VISIBLE);
    }

    private void processReceipts( List<Receipt> receipts ) {
        setNeedsPurchaseText(levelToRadioButton.get(ALLEYWAY), true);
        setNeedsPurchaseText(levelToRadioButton.get(BOXY), true);

        toggleProgressIndicator(false);

        if (receipts == null) {
            return;
        }

        for (Receipt r : receipts) {
            if (r.getIdentifier().equals(getProductIdForLevel(ALLEYWAY))) {
                setNeedsPurchaseText(levelToRadioButton.get(ALLEYWAY), false);
            } else if (r.getIdentifier().equals(getProductIdForLevel(BOXY))) {
                setNeedsPurchaseText(levelToRadioButton.get(BOXY), false);
            }
        }
    }

    private void requestPurchase(final Options.Level level) {
        Purchasable purchasable = new Purchasable(getProductIdForLevel(level));
        OuyaFacade.getInstance().requestPurchase(purchasable, new OuyaResponseListener<Product>() {
            @Override
            public void onSuccess(Product result) {
                if (result.getIdentifier().equals(getProductIdForLevel(level))) {
                    setNeedsPurchaseText(levelToRadioButton.get(level), false);
                    Toast.makeText(OptionsActivity.this, "Level purchased!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                levelToRadioButton.get(FREEDOM).setChecked(true);
                Toast.makeText(OptionsActivity.this, "Error making purchase!\n\nError " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancel() {
                levelToRadioButton.get(FREEDOM).setChecked(true);
                Toast.makeText(OptionsActivity.this, "You cancelled the purchase!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onLevelRadioButtonClicked(View view) {
        RadioButton rb = ((RadioButton) view);
        if (!rb.isChecked()) {
            return;
        }

        if (needsPurchasing(rb)) {
            requestPurchase(radioButtonToLevel.get(rb));
        } else {
            Options.Level level = radioButtonToLevel.get(view);
            getInstance().setLevel(level);
        }
    }
}
