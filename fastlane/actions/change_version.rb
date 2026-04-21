module Fastlane
  module Actions
    class ChangeVersionAction < Action
      def self.run(params)
        gradle = File.read("build.gradle.kts")
          .sub(/val kitshnVersionName by extra\("[0-9a-z\.-]+"\)/, "val kitshnVersionName by extra(\"" + params[:version_name] + "\")")
          .sub(/val kitshnVersionCode by extra\([0-9]+\)/, "val kitshnVersionCode by extra(" + params[:version_code].to_s + ")")
          .sub(/val kitshnAlternateVersionName by extra\("[0-9a-z\.-]+"\)/, "val kitshnAlternateVersionName by extra(\"" + params[:alternate_version_name].to_s + "\")")

        File.write("build.gradle.kts", gradle)
        
        xcodeProject = File.read("iosApp/iosApp.xcodeproj/project.pbxproj")
          .gsub(/CURRENT_PROJECT_VERSION = [0-9]+;/, "CURRENT_PROJECT_VERSION = " + params[:version_code].to_s + ";")
          .gsub(/MARKETING_VERSION = [0-9.]+;/, "MARKETING_VERSION = " + params[:alternate_version_name] + ";")
          
        File.write("iosApp/iosApp.xcodeproj/project.pbxproj", xcodeProject)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.available_options
        [
          "version_name",
          "version_code",
          "alternate_version_name"
        ]
      end

      def self.description
        'Change current kitshn version'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
