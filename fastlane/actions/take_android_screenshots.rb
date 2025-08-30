module Fastlane
  module Actions
    class TakeAndroidScreenshotsAction < Action
      def self.run(params)
        pid = Process.spawn("$ANDROID_HOME/emulator/emulator @#{ params[:avd] } -port 5600 -no-boot-anim -no-snapshot -no-window")

        adb_check = ""

        while adb_check !~ /emulator-5600.*device/
            sleep(1)
            print("Checking status ...")
            adb_check = `$ANDROID_HOME/platform-tools/adb devices`
            puts adb_check
        end

        sleep(10)

        # start demo mode
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell settings put system time_12_24 24"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell settings put global sysui_demo_allowed 1"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command enter"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1337"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command battery -e plugged false"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command battery -e level 100"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e fully true"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e datatype none -e level 4"
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false"

        sleep(5)

        print("Hooray! Device is online.")

        other_action.screengrab(
            device_type: params[:device_type]
        )

        # exit demo mode
        system "$ANDROID_HOME/platform-tools/adb -s emulator-5600 shell am broadcast -a com.android.systemui.demo -e command exit"

        # kill emulator
        Process.kill('QUIT', pid)
        Process.wait(pid)

        metadataScreenshots = "fastlane/metadata/screenshots/en-US/images/#{ params[:device_type] }Screenshots"
        metadataAndroid = "fastlane/metadata/android/en-US/images/#{ params[:device_type] }Screenshots"

        if params[:device_type] == "phone"
            # copy files to android metadata
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_01_HOME.png", "#{ metadataAndroid }/1_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_02_HOME_RECIPE_VIEW.png", "#{ metadataAndroid }/2_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_03_RECIPE_COOKING_MODE.png", "#{ metadataAndroid }/3_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_05_MEAL_PLAN.png", "#{ metadataAndroid }/4_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_07_SHOPPING.png", "#{ metadataAndroid }/5_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_08_SHOPPING_MODE.png", "#{ metadataAndroid }/6_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_09_BOOKS.png", "#{ metadataAndroid }/7_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_10_BOOKS_DETAILS_VIEW.png", "#{ metadataAndroid }/8_en-GB.png")
        else
            # delete unnecessary files (list-detail layout)
            File.delete("#{ metadataScreenshots }/LIGHT_01_HOME.png")
            File.delete("#{ metadataScreenshots }/DARK_01_HOME.png")

            File.delete("#{ metadataScreenshots }/LIGHT_09_BOOKS.png")
            File.delete("#{ metadataScreenshots }/DARK_09_BOOKS.png")

            # copy files to android metadata
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_02_HOME_RECIPE_VIEW.png", "#{ metadataAndroid }/1_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_03_RECIPE_COOKING_MODE.png", "#{ metadataAndroid }/2_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_05_MEAL_PLAN.png", "#{ metadataAndroid }/3_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_07_SHOPPING.png", "#{ metadataAndroid }/4_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_08_SHOPPING_MODE.png", "#{ metadataAndroid }/5_en-GB.png")
            FileUtils.cp("#{ metadataScreenshots }/LIGHT_10_BOOKS_DETAILS_VIEW.png", "#{ metadataAndroid }/6_en-GB.png")
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.available_options
        [
          "avd",
          "device_type"
        ]
      end

      def self.description
        'Start android emulator and take screenshots'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
