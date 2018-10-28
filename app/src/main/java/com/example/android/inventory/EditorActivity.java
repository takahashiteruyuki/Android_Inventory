package com.example.android.inventory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.android.inventory.data.ImageHelper;
import com.example.android.inventory.data.InventoryContract.InventoryEntry;
import java.io.FileNotFoundException;
import java.io.InputStream;
import static com.example.android.inventory.R.id.decrease;
import static com.example.android.inventory.R.id.increase;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_INVENTORY_LOADER = 0;
    private Uri mCurrentInventoryUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mNumberEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private EditText mSupplierEmailEditText;
    private byte[] mImageByteArray;
    private Button mImageButton;
    private ImageView mImageView;
    private static final int IMAGE_PICKER_CODE = 7;
    private int grobalNum;
    private boolean mInventoryHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Button mPhoneButton = (Button) findViewById(R.id.phone_button);
        mPhoneButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mSupplierPhoneEditText.getText().toString().trim()));
                startActivity(intent);
            }
        });
        Button mEmailButton = (Button) findViewById(R.id.email_button);
        mEmailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + mSupplierEmailEditText.getText().toString().trim()));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Recurrent new order");
                String bodyMessage = "Please send us as soon as possible more " +
                        mNameEditText.getText().toString().trim() +
                        "!!!";
                intent.putExtra(android.content.Intent.EXTRA_TEXT, bodyMessage);
                startActivity(intent);
            }
        });
        Button mIncrease = (Button) findViewById(increase);
        mIncrease.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                grobalNum++;
                EditText mNumberEditText2 = (EditText) findViewById(R.id.edit_inventory_number);
                mNumberEditText2.setText(Integer.toString(grobalNum));
            }
        });
        Button mDecrease = (Button) findViewById(decrease);
        mDecrease.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(grobalNum>0) {
                    grobalNum--;
                    EditText mNumberEditText2 = (EditText) findViewById(R.id.edit_inventory_number);
                    mNumberEditText2.setText(Integer.toString(grobalNum));
                }
            }
        });
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        if (mCurrentInventoryUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_inventory));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_inventory));
            getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        }
        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mNumberEditText = (EditText) findViewById(R.id.edit_inventory_number);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_inventory_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_inventory_supplier_phone);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_inventory_supplier_email);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mImageView.setVisibility(View.GONE);
        mImageButton = (Button) findViewById(R.id.image_button);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Intent chooserIntent = Intent.createChooser(intent, "Select an Image");
                startActivityForResult(chooserIntent, IMAGE_PICKER_CODE);
                mImageView.setVisibility(View.VISIBLE);
            }
        });
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mNumberEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
    }
    private void saveInventory() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String numberString = mNumberEditText.getText().toString().trim();
        String supplierNameString = mSupplierEmailEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        if (mCurrentInventoryUri == null &&(
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(numberString) || TextUtils.isEmpty(supplierNameString                )
                || TextUtils.isEmpty(supplierPhoneString) || TextUtils.isEmpty(supplierEmailString)
        ||mImageByteArray == null)) {
            Toast.makeText(this, "Please enter all data!!",Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);
        int number = 0;
        if (!TextUtils.isEmpty(numberString)) {
            number = Integer.parseInt(numberString);
        }
        values.put(InventoryEntry.COLUMN_INVENTORY_NUMBER, number);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE, supplierPhoneString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, supplierEmailString);
        if (mImageByteArray != null) {
            values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, mImageByteArray);
        }
        if (mCurrentInventoryUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveInventory();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_NUMBER,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL,
                InventoryEntry.COLUMN_INVENTORY_IMAGE
        };
        return new CursorLoader(this,
                mCurrentInventoryUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int numberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NUMBER);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int number = cursor.getInt(numberColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            mImageByteArray = cursor.getBlob(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE));
            Bitmap inventoryImage = ImageHelper.convertBlobToBitmap(mImageByteArray);
            if (inventoryImage != null) {
                mImageView.setImageBitmap(inventoryImage);
                mImageView.setVisibility(View.VISIBLE);
            } else {
                mImageView.setVisibility(View.GONE);
            }
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mNumberEditText.setText(Integer.toString(number));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);
            mSupplierEmailEditText.setText(supplierEmail);
            grobalNum=number;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mNumberEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mSupplierEmailEditText.setText("");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mImageView.setImageBitmap(selectedImage);
                // Temp code to store image
                mImageByteArray = ImageHelper.convertBitmapToBlob(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(EditorActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == IMAGE_PICKER_CODE) {
            Toast.makeText(EditorActivity.this, "You haven't picked an image", Toast.LENGTH_LONG).show();
        }
    }
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void deleteInventory() {
        if (mCurrentInventoryUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_inventory_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}