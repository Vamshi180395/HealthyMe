package com.example.healthyMe;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rama Vamshi Krishna on 11/14/2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static List<Message> list_of_all_messages;
    private Context mContext;
    private static HomeActivity activity;
    private PrettyTime prettyTime=new PrettyTime();
    private static final int MESSAGE_INFORMATIOAL = 0, MESSAGE_SIMPLE_QUESTION = 1, MESSAGE_SURVEY_QUESTION = 2;

    public MessagesAdapter(Context context, HomeActivity activity, List<Message> list_of_all_messages) {
        this.mContext = context;
        this.activity = activity;
        this.list_of_all_messages = list_of_all_messages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_INFORMATIOAL) {
            View imsg_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_informational_question, null);
            return new InformationalQuestionViewHolder(imsg_row);
        } else if (viewType == MESSAGE_SIMPLE_QUESTION) {
            View smsg_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_simple_question, null);
            return new SimpleQuestionViewHolder(smsg_row);
        } else {
            View sur_msg_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_survey_question, null);
            return new SurveyQuestionViewHolder(sur_msg_row);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int itemType = getItemViewType(position);
        Message current_msg = list_of_all_messages.get(position);
        if (current_msg != null) {
            if (itemType == MESSAGE_INFORMATIOAL) {
                ((InformationalQuestionViewHolder) holder).txt_info_msg.setText(current_msg.getMsg_txt());
                ((InformationalQuestionViewHolder) holder).txt_msg_prettytime.setText(prettyTime.format(current_msg.getPublishedDate()));
            } else if (itemType == MESSAGE_SIMPLE_QUESTION) {
                if (!current_msg.getUserHasReponded()) {
                    ((SimpleQuestionViewHolder) holder).rg_yesorno.setVisibility(View.VISIBLE);
                    ((SimpleQuestionViewHolder) holder).btnsubmit_answer.setVisibility(View.VISIBLE);
                    ((SimpleQuestionViewHolder) holder).txt_answer.setVisibility(View.GONE);
                    ((SimpleQuestionViewHolder) holder).txt_question.setText(current_msg.getMsg_txt());
                    ((SimpleQuestionViewHolder) holder).txt_msg_prettytime.setText(prettyTime.format(current_msg.getPublishedDate()));
                } else {
                    ((SimpleQuestionViewHolder) holder).rg_yesorno.setVisibility(View.GONE);
                    ((SimpleQuestionViewHolder) holder).btnsubmit_answer.setVisibility(View.GONE);
                    ((SimpleQuestionViewHolder) holder).txt_answer.setVisibility(View.VISIBLE);
                    ((SimpleQuestionViewHolder) holder).txt_question.setText(current_msg.getMsg_txt());
                    ((SimpleQuestionViewHolder) holder).txt_answer.setText("Responded with : " + current_msg.getMsg_answer());
                    ((SimpleQuestionViewHolder) holder).txt_msg_prettytime.setText(prettyTime.format(current_msg.getPublishedDate()));
                }
            } else {
                if (!current_msg.getUserHasReponded()) {
                    ((SurveyQuestionViewHolder) holder).txt_answer.setVisibility(View.GONE);
                    ((SurveyQuestionViewHolder) holder).txt_question.setText(current_msg.getMsg_txt());
                    ((SurveyQuestionViewHolder) holder).txt_msg_prettytime.setText(prettyTime.format(current_msg.getPublishedDate()));
                    ((SurveyQuestionViewHolder) holder).btn_start_survey.setVisibility(View.VISIBLE);
                } else {
                    ((SurveyQuestionViewHolder) holder).btn_start_survey.setVisibility(View.GONE);
                    ((SurveyQuestionViewHolder) holder).txt_answer.setVisibility(View.VISIBLE);
                    ((SurveyQuestionViewHolder) holder).txt_question.setText(current_msg.getMsg_txt());
                    ((SurveyQuestionViewHolder) holder).txt_msg_prettytime.setText(prettyTime.format(current_msg.getPublishedDate()));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list_of_all_messages.get(position).getType() == 1) {
            return MESSAGE_INFORMATIOAL;
        } else if (list_of_all_messages.get(position).getType() == 2) {
            return MESSAGE_SIMPLE_QUESTION;
        } else {
            return MESSAGE_SURVEY_QUESTION;
        }
    }

    @Override
    public int getItemCount() {
        return list_of_all_messages.size();
    }


    public static class InformationalQuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_info_msg, txt_msg_prettytime;
        public InformationalQuestionViewHolder(View itemView) {
            super(itemView);
            txt_info_msg = (TextView) itemView.findViewById(R.id.txt_infomsg);
            txt_msg_prettytime= (TextView) itemView.findViewById(R.id.txt_msgprettytime);
        }
    }

    public static class SimpleQuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_question, txt_answer, txt_msg_prettytime;
        private RadioGroup rg_yesorno;
        private RadioButton selected_radio_button;
        private String user_radio_answer;
        Button btnsubmit_answer;

        public SimpleQuestionViewHolder(View itemView) {
            super(itemView);
            txt_question = (TextView) itemView.findViewById(R.id.question_txt);
            txt_answer = (TextView) itemView.findViewById(R.id.answer_txt);
            btnsubmit_answer = (Button) itemView.findViewById(R.id.btn_submitanswer);
            txt_msg_prettytime = (TextView) itemView.findViewById(R.id.txt_msgprettytime);
            rg_yesorno = (RadioGroup) itemView.findViewById(R.id.radioGroup);
            rg_yesorno.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    View radioButton = radioGroup.findViewById(i);
                    int idx = radioGroup.indexOfChild(radioButton);
                    selected_radio_button = (RadioButton) rg_yesorno.getChildAt(idx);
                    user_radio_answer = selected_radio_button.getText().toString();
                }
            });
            btnsubmit_answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    MessagesAdapterInterface simple_question_interface = (HomeFragment) activity.getSupportFragmentManager().findFragmentByTag("Home_Fragment");
                    simple_question_interface.submitResponseForSimpleQuestion(list_of_all_messages.get(position).getMsg_id(), user_radio_answer);
                }
            });
        }

    }

    public static class SurveyQuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_question, txt_answer, txt_msg_prettytime;
        private Button btn_start_survey;

        public SurveyQuestionViewHolder(View itemView) {
            super(itemView);
            txt_question = (TextView) itemView.findViewById(R.id.question_txt);
            txt_answer = (TextView) itemView.findViewById(R.id.answer_txt);
            txt_msg_prettytime= (TextView) itemView.findViewById(R.id.txt_msgprettytime);
            btn_start_survey = (Button) itemView.findViewById(R.id.btn_startsurvey);
            btn_start_survey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    MessagesAdapterInterface survey_question_interface = (HomeFragment) activity.getSupportFragmentManager().findFragmentByTag("Home_Fragment");
                    survey_question_interface.startSurveyForSurveyQuestion(list_of_all_messages.get(position).getMsg_id());
                }
            });

        }

    }

    public interface MessagesAdapterInterface {
        public void submitResponseForSimpleQuestion(String selected_message_id, String answer);
        public void startSurveyForSurveyQuestion(String selected_message_id);
    }

    public void setFilter(List<Message> filtered_messages) {
        list_of_all_messages = new ArrayList<>();
        list_of_all_messages.addAll(filtered_messages);
        notifyDataSetChanged();
    }
}



