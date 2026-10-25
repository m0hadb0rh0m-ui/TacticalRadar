package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.*;
import android.util.Patterns;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class AuthActivity extends Activity {
 private static final int BG=0xff070B14,CYAN=0xff42E8F5,WHITE=0xffF4F8FF,MUTED=0xff9FB5CA,GOLD=0xffFFCC75;
 private LinearLayout root,form;private EditText email,password,confirm;private boolean register=false;private TextView heading,subheading,modeLink;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
 private GradientDrawable background(int a,int b,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(radius));d.setStroke(dp(1),0xff2B5068);return d;}
 private void gap(LinearLayout c,int h){View v=new View(this);c.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 private EditText field(String hint,int type){
  EditText e=new EditText(this);e.setHint(hint);e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setTextSize(15);e.setSingleLine(true);e.setInputType(type);e.setPadding(dp(16),dp(12),dp(16),dp(12));e.setBackground(background(0xff1B2A42,0xff132139,14));
  form.addView(e,new LinearLayout.LayoutParams(-1,dp(54)));gap(form,12);return e;
 }
 private Button button(String title,boolean primary){
  Button b=new Button(this);b.setText(title);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(primary?BG:WHITE);
  GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,primary?new int[]{0xff41D8E8,0xff77F4F2}:new int[]{0xff203B55,0xff15263C});d.setCornerRadius(dp(15));b.setBackground(d);return b;
 }
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
  root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(25),dp(34),dp(25),dp(30));
  root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff07101C,0xff0D2237,0xff080B14}));scroll.addView(root);setContentView(scroll);
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.titan_logo);root.addView(logo,new LinearLayout.LayoutParams(dp(108),dp(108)));
  TextView brand=text("TITAN CORE X",29,WHITE,true);brand.setGravity(Gravity.CENTER);brand.setLetterSpacing(.08f);root.addView(brand);
  TextView tagline=text("SECURE ACCESS  •  NEXT-GEN INTELLIGENCE",10,CYAN,true);tagline.setGravity(Gravity.CENTER);root.addView(tagline);gap(root,28);
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);panel.setPadding(dp(19),dp(23),dp(19),dp(20));panel.setBackground(background(0xff182C44,0xff0E1A2D,24));root.addView(panel,new LinearLayout.LayoutParams(-1,-2));
  heading=text("",23,WHITE,true);panel.addView(heading);gap(panel,5);
  subheading=text("",13,MUTED,false);panel.addView(subheading);gap(panel,21);
  form=new LinearLayout(this);form.setOrientation(1);panel.addView(form);
  modeLink=text("",13,CYAN,true);modeLink.setGravity(Gravity.CENTER);modeLink.setPadding(0,dp(15),0,dp(12));panel.addView(modeLink);
  modeLink.setOnClickListener(v->{register=!register;renderForm();});
  gap(root,19);TextView privacy=text("خصوصيتك أولًا • لا نطلب كلمة مرور Google داخل التطبيق",12,MUTED,false);privacy.setGravity(Gravity.CENTER);root.addView(privacy);
  renderForm();
 }
 private void renderForm(){
  heading.setText(register?"إنشاء حساب جديد":"مرحبًا بعودتك");
  subheading.setText(register?"أنشئ حساب TITAN CORE X عندما تتوفر خدمة الحسابات":"سجّل الدخول إلى تجربة TITAN CORE X");
  modeLink.setText(register?"لديك حساب بالفعل؟ تسجيل الدخول":"ليس لديك حساب؟ إنشاء حساب");
  form.removeAllViews();
  email=field("البريد الإلكتروني",android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
  password=field("كلمة المرور",android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
  if(register)confirm=field("تأكيد كلمة المرور",android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
  LinearLayout visibility=new LinearLayout(this);visibility.setGravity(Gravity.RIGHT);
  CheckBox reveal=new CheckBox(this);reveal.setText("إظهار كلمة المرور");reveal.setTextColor(MUTED);visibility.addView(reveal);form.addView(visibility);
  reveal.setOnCheckedChangeListener((b,checked)->{
    int type=android.text.InputType.TYPE_CLASS_TEXT|(checked?android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    password.setInputType(type);if(register&&confirm!=null)confirm.setInputType(type);
  });
  gap(form,10);
  Button submit=button(register?"إنشاء الحساب":"تسجيل الدخول",true);form.addView(submit,new LinearLayout.LayoutParams(-1,dp(53)));
  submit.setOnClickListener(v->validate());
  gap(form,14);TextView or=text("────────  أو المتابعة باستخدام  ────────",12,MUTED,false);or.setGravity(Gravity.CENTER);form.addView(or);gap(form,14);
  Button google=button("G   المتابعة باستخدام Google",false);form.addView(google,new LinearLayout.LayoutParams(-1,dp(52)));
  google.setOnClickListener(v->unavailable("Google Sign-In"));
  gap(form,14);
  Button guest=button("الدخول كضيف بدون حساب",false);form.addView(guest,new LinearLayout.LayoutParams(-1,dp(52)));
  guest.setOnClickListener(v->openApp());
  gap(form,12);
  TextView note=text("تسجيل Google والبريد الإلكتروني يتطلبان إعداد Firebase Authentication أو خادم مصادقة موثوق. هذه الواجهة جاهزة للربط، لكن إنشاء الحسابات وتسجيل الدخول غير مفعّلين بعد.",11,GOLD,false);
  form.addView(note);
 }
 private void validate(){
  String mail=email.getText().toString().trim(),pass=password.getText().toString();
  if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){email.setError("أدخل بريدًا إلكترونيًا صحيحًا");return;}
  if(pass.length()<8){password.setError("كلمة المرور يجب أن تحتوي 8 أحرف على الأقل");return;}
  if(register&&!pass.equals(confirm.getText().toString())){confirm.setError("كلمتا المرور غير متطابقتين");return;}
  unavailable(register?"إنشاء الحساب":"تسجيل الدخول");
 }
 private void unavailable(String feature){
  new AlertDialog.Builder(this).setTitle(feature).setMessage("واجهة "+feature+" جاهزة. لإتمام المصادقة الحقيقية نحتاج إعداد مشروع Firebase وربط Google OAuth والتحقق من البريد الإلكتروني. لا تُخزّن كلمات المرور محليًا ولا تُرسل إلى جهة غير موثوقة. يمكنك متابعة استخدام التطبيق كضيف.").setNegativeButton("إغلاق",null).setPositiveButton("الدخول كضيف",(d,w)->openApp()).show();
 }
 private void openApp(){startActivity(new Intent(this,MainActivity.class));finish();}
}
