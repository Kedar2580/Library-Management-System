package com.library.service;

import com.library.model.Setting;
import com.library.repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SettingsService {

    private final SettingRepository settingRepository;

    public SettingsService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public String get(String key, String defaultValue) {
        return settingRepository.findByKey(key)
                .map(Setting::getValue)
                .orElse(defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void set(String key, String value) {
        Setting setting = settingRepository.findByKey(key).orElseGet(() -> {
            Setting s = new Setting();
            s.setKey(key);
            return s;
        });
        setting.setValue(value);
        settingRepository.save(setting);
    }

    public Map<String, String> all() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(Setting::getKey, Setting::getValue));
    }
}
