package com.ericbandiero.ratsandmice.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.R;

import com.ericbandiero.myframework.Utility;

public class HelpActivity extends ActionBarActivity {

    int helpType=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_new);

    //TODO Optimize this by fixing names and adding html?


        int id = getIntent().getIntExtra("menu", -1);
        String title=getIntent().getStringExtra("title");
        String header=getIntent().getStringExtra("heading");
        String menu_resource_name=getIntent().getStringExtra("menu_resource_name");

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","Menu id:"+id );
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","Menu title:"+title );
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","Menu resource name:"+menu_resource_name );

        TextView textViewHelp = (TextView) findViewById(R.id.lbl_help);
        textViewHelp.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textViewTitle = (TextView) findViewById(R.id.lbl_help_title);
        textViewTitle.setGravity(Gravity.CENTER);

        TextView textViewLink = (TextView) findViewById(R.id.lbl_help_link);
        textViewLink.setVisibility(View.INVISIBLE);

        int identifier = getResources().getIdentifier(menu_resource_name+"_html", "string", getPackageName());

        if (id==R.id.help_help) {
            setTitle(title);
            textViewTitle.setText("How to Use");
            //Spanned link=Html.fromHtml(getString(R.string.dohmh_link));
            //textViewHelp.setText(Html.fromHtml(getString(R.string.help)));


            textViewHelp.setText(Html.fromHtml(getString(identifier)));
        }

        if (id==R.id.links_help) {
            setTitle(title);
            textViewTitle.setText("Links");
            //textViewHelp.setText(Html.fromHtml(getString(R.string.quick_help_html)+getString(R.string.dohmh_link)));
            textViewHelp.setText(Html.fromHtml(getString(identifier)));
        }

        if (id==R.id.search_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Using Search");
            //textViewHelp.setText(Html.fromHtml(getString(R.string.help_search_html)));
            textViewHelp.setText(Html.fromHtml(getString(identifier)));
        }

        if (id==R.id.gut_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Gut Feeling Info");
            textViewHelp.setText(Html.fromHtml(getString(R.string.help_gut_html)));
        }

        if (id==R.id.symbols_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Symbols");
            textViewHelp.setText(Html.fromHtml(getString(R.string.help_symbols_html)));
        }

        if (id==R.id.mission_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Mission Statement");
            textViewHelp.setText(Html.fromHtml(getString(R.string.help_mission_html)));
        }

        if (id==R.id.disclaimer_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Disclaimer");
            textViewHelp.setText(Html.fromHtml(getString(identifier)));
        }

        if (id==R.id.features_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Features");
            textViewHelp.setText(Html.fromHtml(getString(identifier)));
        }

        if (id==R.id.contact_feedback_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Contact and Feedback");
            textViewHelp.setText(Html.fromHtml(getString(identifier)));

            textViewLink.setVisibility(View.VISIBLE);

            textViewLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String websiteOrEmail;
                    Intent newIntent;
                    websiteOrEmail = getString(R.string.Email);
                    newIntent = new Intent(Intent.ACTION_SEND);
                    newIntent.setData(Uri.parse("mailto:"));
                    newIntent.setType("text/html");
                    newIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{websiteOrEmail});
                    newIntent.putExtra(Intent.EXTRA_SUBJECT, "Dancer app feedback");
                    startActivity(Intent.createChooser(newIntent,
                            "Send email"));
                }
            });
        }

        if (id==R.id.about_help) {
            setTitle(title);
            helpType=1;
            textViewTitle.setText("Version:"+ UtilsShared.getVersion(getApplicationContext()));
            textViewTitle.setGravity(Gravity.CENTER);
            textViewHelp.setText(Html.fromHtml(getString(identifier)));

        }
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if(id==android.R.id.home & helpType==1){
//            startActivity(new Intent(this, SearchActivity.class));
//            return true;
//        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
