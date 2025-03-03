module Fastlane
  module Actions
    class RetrieveVersionAction < Action
      def self.run(params)
        version = File.read("composeApp/build.gradle.kts").split("val kitshnVersionName = \"", 2)[1].split("\"")[0]
        UI.message("Found kitshn version: #{version}")
        return version
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Retrieving current kitshn version from composeApp/build.gradle.kts'
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
